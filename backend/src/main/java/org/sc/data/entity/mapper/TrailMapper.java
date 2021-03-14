package org.sc.data.entity.mapper;

import org.bson.Document;
import org.sc.data.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class TrailMapper implements Mapper<Trail> {

    protected final PlaceRefMapper placeMapper;
    protected final TrailCoordinatesMapper trailCoordinatesMapper;
    protected final GeoLineMapper geoLineMapper;
    protected final StatsTrailMapper statsTrailMapper;
    private final LinkedMediaMapper linkedMediaMapper;

    @Autowired
    public TrailMapper(final PlaceRefMapper placeMapper,
                       final TrailCoordinatesMapper trailCoordinatesMapper,
                       final GeoLineMapper geoLineMapper,
                       final StatsTrailMapper statsTrailMapper,
                       final LinkedMediaMapper linkedMediaMapper) {
        this.placeMapper = placeMapper;
        this.trailCoordinatesMapper = trailCoordinatesMapper;
        this.geoLineMapper = geoLineMapper;
        this.statsTrailMapper = statsTrailMapper;
        this.linkedMediaMapper = linkedMediaMapper;
    }

    @Override
    public Trail mapToObject(final Document doc) {
        return Trail.builder()
                .id(doc.getString(Trail.ID))
                .name(doc.getString(Trail.NAME))
                .description(doc.getString(Trail.DESCRIPTION))
                .code(doc.getString(Trail.CODE))
                .officialEta(doc.getInteger(Trail.OFFICIAL_ETA))
                .variant(doc.getBoolean(Trail.VARIANT))
                .locations(getLocations(doc))
                .classification(getClassification(doc))
                .statsTrailMetadata(getMetadata(doc.get(Trail.STATS_METADATA, Document.class)))
                .country(doc.getString(Trail.COUNTRY))
                .coordinates(getCoordinatesWithAltitude(doc))
                .createdOn(getCreatedDate(doc))
                .lastUpdate(getLastUpdateDate(doc))
                .maintainingSection(doc.getString(Trail.SECTION_CARED_BY))
                .territorialDivision(doc.getString(Trail.TERRITORIAL_CARED_BY))
                .geoLineString(getGeoLine(doc.get(Trail.GEO_LINE, Document.class)))
                .mediaList(getLinkedMediaMapper(doc))
                .build();
    }

    @Override
    public Document mapToDocument(final Trail object) {
        return new Document()
                .append(Trail.NAME, object.getName())
                .append(Trail.DESCRIPTION, object.getDescription())
                .append(Trail.CODE, object.getCode())
                .append(Trail.OFFICIAL_ETA, object.getOfficialEta())
                .append(Trail.LOCATIONS, object.getLocations().stream()
                        .map(placeMapper::mapToDocument).collect(toList()))
                .append(Trail.CLASSIFICATION, object.getClassification().toString())
                .append(Trail.COUNTRY, object.getCountry())
                .append(Trail.SECTION_CARED_BY, object.getMaintainingSection())
                .append(Trail.LAST_UPDATE_DATE, new Date())
                .append(Trail.VARIANT, object.isVariant())
                .append(Trail.TERRITORIAL_CARED_BY, object.getTerritorialDivision())
                .append(Trail.STATS_METADATA, statsTrailMapper.mapToDocument(object.getStatsTrailMetadata()))
                .append(Trail.COORDINATES, object.getCoordinates().stream()
                        .map(trailCoordinatesMapper::mapToDocument).collect(toList()))
                .append(Trail.MEDIA, object.getMediaList().stream()
                        .map(linkedMediaMapper::mapToDocument)
                        .collect(toList()))
                .append(Trail.GEO_LINE, geoLineMapper.mapToDocument(object.getGeoLineString()));
    }

    protected GeoLineString getGeoLine(final Document doc) {
        return geoLineMapper.mapToObject(doc);
    }

    protected StatsTrailMetadata getMetadata(final Document doc) {
        return new StatsTrailMetadata(doc.getDouble(StatsTrailMetadata.TOTAL_RISE),
                doc.getDouble(StatsTrailMetadata.TOTAL_FALL),
                doc.getDouble(StatsTrailMetadata.ETA),
                doc.getDouble(StatsTrailMetadata.LENGTH));
    }

    protected List<LinkedMedia> getLinkedMediaMapper(Document doc) {
        List<Document> list = doc.getList(Poi.MEDIA, Document.class);
        return list.stream().map(linkedMediaMapper::mapToObject).collect(toList());
    }

    private List<TrailCoordinates> getCoordinatesWithAltitude(final Document doc) {
        final List<Document> list = doc.getList(Trail.COORDINATES, Document.class);
        return list.stream().map(trailCoordinatesMapper::mapToObject).collect(toList());
    }

    protected List<PlaceRef> getLocations(final Document doc) {
        List<Document> list = doc.getList(Trail.LOCATIONS, Document.class);
        return list.stream().map(placeMapper::mapToObject).collect(toList());
    }

    protected PlaceRef getPos(final Document doc,
                           final String fieldName) {
        final Document pos = doc.get(fieldName, Document.class);
        return placeMapper.mapToObject(pos);
    }

    protected Date getLastUpdateDate(Document doc) {
        return doc.getDate(Trail.LAST_UPDATE_DATE);
    }

    protected Date getCreatedDate(Document doc) {
        return doc.getDate(Trail.CREATED_ON_DATE);
    }

    protected TrailClassification getClassification(Document doc) {
        final String classification = doc.getString(Trail.CLASSIFICATION);
        return TrailClassification.valueOf(classification);
    }

}
