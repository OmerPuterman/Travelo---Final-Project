package com.example.demo.model;

import java.util.HashSet;
import java.util.Set;

import ch.hsr.geohash.GeoHash;

public class GeoHashUtil {

    public static String encode(double lat, double lng) {
        return GeoHash.geoHashStringWithCharacterPrecision(
                lat,
                lng,
                6 // precision 1.2km x 0.6km
        );
    }
    public static Set<String> getGeoHashGrid(double lat, double lon) {

        GeoHash center = GeoHash.withCharacterPrecision(lat, lon, 6);

        Set<String> grid = new HashSet<>();

        grid.add(center.toBase32());

        for (GeoHash neighbor : center.getAdjacent()) {
            grid.add(neighbor.toBase32());
        }

        return grid;
    }
}