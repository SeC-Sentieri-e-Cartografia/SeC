package org.sc.data.repository;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.sc.configuration.DataSource;
import org.sc.data.model.LinkedMedia;
import org.sc.data.entity.mapper.LinkedMediaMapper;
import org.sc.data.entity.mapper.PoiMapper;
import org.sc.data.model.Poi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.sc.data.repository.MongoConstants.*;

@Repository
public class PoiDAO {


    private final MongoCollection<Document> collection;
    private final PoiMapper mapper;
    private final LinkedMediaMapper linkedMediaMapper;

    @Autowired
    public PoiDAO(final DataSource dataSource,
                  final PoiMapper mapper,
                  final LinkedMediaMapper linkedMediaMapper) {
        this.collection = dataSource.getDB().getCollection(Poi.COLLECTION_NAME);
        this.mapper = mapper;
        this.linkedMediaMapper = linkedMediaMapper;
    }

    public List<Poi> get(final int page,
                         final int count) {
        return toPoisList(collection.find().skip(page).limit(count));
    }

    public List<Poi> getById(final String id) {
        return toPoisList(collection.find(new Document(Poi.OBJECT_ID, id)));
    }

    public List<Poi> getByCode(final String code,
                               final int page,
                               final int count) {
        return toPoisList(collection.find(new Document(Poi.TRAIL_CODES, code)).skip(page).limit(count));
    }

    public List<Poi> getByMacro(final String macroType,
                                final int page,
                                final int count) {
        return toPoisList(collection.find(new Document(Poi.MACROTYPE, macroType)).skip(page).limit(count));
    }

    public List<Poi> getByName(final String name,
                               int page,
                               int count) {
        return toPoisList(collection.find(new Document(Poi.NAME, name)).skip(page).limit(count));
    }

    public List<Poi> getByTags(final String tag,
                               final int page,
                               final int count) {
        return toPoisList(collection.find(new Document(Poi.TAGS, tag)).skip(page).limit(count));
    }

    @NotNull
    public List<Poi> getByPosition(double longitude, double latitude, double meters, int page, int count) {
        final AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(new Document($_GEO_NEAR_OPERATOR,
                        new Document(NEAR_OPERATOR, new Document("type", "Point")
                                .append("coordinates", Arrays.asList(longitude, latitude)))
                                .append(DISTANCE_FIELD, "distanceToIt")
                                .append(KEY_FIELD, "coordinates.coordinates")
                                .append(INCLUDE_LOCS_FIELD, "closestLocation")
                                .append(MAX_DISTANCE_M, meters)
                                .append(SPHERICAL_FIELD, "true")
                                .append(UNIQUE_DOCS_FIELD, "true")),
                new Document(SKIP, page),
                new Document(LIMIT, count)
        ));
        return toPoisList(aggregate);
    }

    public List<Poi> upsert(final Poi poiRequest) {
        // TODO: shall upsert and update the date
        final Document poiDoc = mapper.mapToDocument(poiRequest);
        final String existingOrNewObjectId = poiRequest.get_id() == null ?
                new ObjectId().toHexString() : poiRequest.get_id();
        final Document updateResult = collection.findOneAndReplace(
                new Document(Poi.OBJECT_ID, existingOrNewObjectId),
                poiDoc, new FindOneAndReplaceOptions().upsert(true)
                        .returnDocument(ReturnDocument.AFTER));
        if (updateResult != null) {
            return Collections.singletonList(mapper.mapToObject(updateResult));
        }
        throw new IllegalStateException();
    }

    public List<Poi> delete(final String id) {
        final List<Poi> byId = getById(id);
        collection.deleteOne(new Document(Poi.OBJECT_ID, id));
        return byId;
    }

    public List<Poi> linkMedia(final String id,
                               final LinkedMedia linkMedia) {
        collection.updateOne(new Document(Poi.OBJECT_ID, id), new Document(ADD_TO_SET,
                new Document(Poi.MEDIA, linkedMediaMapper.mapToDocument(linkMedia))));
        return getById(id);
    }

    public List<Poi> unlinkMediaId(final String id, final String mediaId) {
        collection.updateOne(new Document(Poi.OBJECT_ID, id),
                new Document(MongoConstants.PULL,
                        new Document(Poi.MEDIA, new Document(LinkedMedia.ID, mediaId))));
        return getById(id);
    }

    public void unlinkMediaByAllPoi(final String mediaId) {
        collection.updateOne(new Document(),
                new Document(MongoConstants.PULL, new Document(Poi.MEDIA, mediaId)));
    }

    private List<Poi> toPoisList(final Iterable<Document> documents) {
        return StreamSupport.stream(documents.spliterator(), false).map(mapper::mapToObject).collect(toList());
    }

    public long countPOI() {
        return collection.countDocuments();
    }

}
