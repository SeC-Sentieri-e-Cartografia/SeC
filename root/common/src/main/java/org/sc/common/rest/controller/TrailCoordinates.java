package org.sc.common.rest.controller;

import java.util.Objects;
import java.util.stream.Stream;

public class TrailCoordinates extends CoordinatesWithAltitude {

    public final static String DISTANCE_FROM_START = "distFromStart";

    private final double distanceFromTrailStart;

    public TrailCoordinates(final double longitude,
                            final double latitude,
                            final double altitude,
                            final double distanceFromTrailStart) {
        super(longitude, latitude, altitude);
        this.distanceFromTrailStart = distanceFromTrailStart;
    }

    public double getDistanceFromTrailStart() {
        return distanceFromTrailStart;
    }

    public static final class TrailCoordinatesBuilder {
        private double distanceFromTrailStart;
        private double altitude;
        private double longitude;
        private double latitude;

        private TrailCoordinatesBuilder() {
        }

        public static TrailCoordinatesBuilder aTrailCoordinates() {
            return new TrailCoordinatesBuilder();
        }

        public TrailCoordinatesBuilder withDistanceFromTrailStart(double distanceFromTrailStart) {
            this.distanceFromTrailStart = distanceFromTrailStart;
            return this;
        }

        public TrailCoordinatesBuilder withAltitude(final double altitude) {
            this.altitude = altitude;
            return this;
        }

        public TrailCoordinatesBuilder withLongitude(final double longitude) {
            this.longitude = longitude;
            return this;
        }

        public TrailCoordinatesBuilder withLatitude(final double latitude) {
            this.latitude = latitude;
            return this;
        }

        public TrailCoordinates build() {
            assertCorrectValues();
            return new TrailCoordinates(longitude, latitude, altitude, distanceFromTrailStart);
        }

        private void assertCorrectValues() {
            if(Stream.of(distanceFromTrailStart, altitude, longitude, latitude).anyMatch(Objects::isNull)){
                throw new IllegalArgumentException(CoordinatesWithAltitude.NO_CORRECT_PARAMS_ERROR_MESSAGE);
            }
        }
    }
}
