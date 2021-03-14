package org.sc.data.entity.mapper;

import org.bson.Document;
import org.sc.data.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrailPreviewMapper implements Mapper<TrailPreview> {

    private final PlaceRefMapper placeMapper;

    @Autowired
    public TrailPreviewMapper(final PlaceRefMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    @Override
    public TrailPreview mapToObject(Document doc) {
        return new TrailPreview(doc.getString(Trail.CODE),
                getClassification(doc),
                getPos(doc, Trail.START_POS),
                getPos(doc, Trail.FINAL_POS),
                doc.getDate(Trail.LAST_UPDATE_DATE));
    }

    @Override
    public Document mapToDocument(TrailPreview object) {
        throw new IllegalStateException();
    }

    private PlaceRef getPos(final Document doc,
                            final String fieldName) {
        final Document pos = doc.get(fieldName, Document.class);
        return placeMapper.mapToObject(pos);
    }

    private TrailClassification getClassification(Document doc) {
        final String classification = doc.getString(Trail.CLASSIFICATION);
        return TrailClassification.valueOf(classification);
    }
}
