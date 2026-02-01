package com.nightskylab.data;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages constellation line and name data.
 */
public class ConstellationData {

    /**
     * Represents a single constellation.
     */
    public static class Constellation {
        public String id; // e.g., "UMa"
        public String name; // e.g., "Ursa Major"
        public List<List<Point>> lines; // Line segments
        public Point centroid; // Label position

        public Constellation(String id, String name) {
            this.id = id;
            this.name = name;
            this.lines = new ArrayList<>();
        }
    }

    /**
     * A point in RA/Dec coordinates.
     */
    public static class Point {
        public double ra; // degrees
        public double dec; // degrees

        public Point(double ra, double dec) {
            this.ra = ra;
            this.dec = dec;
        }
    }

    /**
     * JSON structure for constellation data.
     */
    private static class ConstellationJson {
        @SerializedName("features")
        List<Feature> features;

        static class Feature {
            String id;
            Geometry geometry;
            Properties properties;
        }

        static class Geometry {
            String type;
            List<List<List<Double>>> coordinates; // MultiLineString format
        }

        static class Properties {
            String name;
        }
    }

    private List<Constellation> constellations;
    private Map<String, String> nameMap;

    public ConstellationData() {
        constellations = new ArrayList<>();
        initializeNameMap();
    }

    /**
     * Load constellation data from raw resource.
     */
    public void loadFromResource(Context context, int resourceId) {
        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            InputStreamReader reader = new InputStreamReader(is);

            Gson gson = new Gson();
            ConstellationJson data = gson.fromJson(reader, ConstellationJson.class);

            constellations.clear();

            for (ConstellationJson.Feature feature : data.features) {
                String id = feature.id;
                String name = nameMap.getOrDefault(id, id);

                Constellation constellation = new Constellation(id, name);

                // Parse line coordinates
                if (feature.geometry != null && feature.geometry.coordinates != null) {
                    for (List<List<Double>> lineString : feature.geometry.coordinates) {
                        List<Point> line = new ArrayList<>();
                        for (List<Double> coord : lineString) {
                            if (coord.size() >= 2) {
                                line.add(new Point(coord.get(0), coord.get(1)));
                            }
                        }
                        if (!line.isEmpty()) {
                            constellation.lines.add(line);
                        }
                    }
                }

                // Calculate centroid from lines
                calculateCentroid(constellation);

                constellations.add(constellation);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate centroid for constellation label placement.
     */
    private void calculateCentroid(Constellation constellation) {
        double sumRa = 0;
        double sumDec = 0;
        int count = 0;

        for (List<Point> line : constellation.lines) {
            for (Point point : line) {
                sumRa += point.ra;
                sumDec += point.dec;
                count++;
            }
        }

        if (count > 0) {
            constellation.centroid = new Point(sumRa / count, sumDec / count);
        } else {
            constellation.centroid = new Point(0, 0);
        }
    }

    /**
     * Get all constellations.
     */
    public List<Constellation> getConstellations() {
        return constellations;
    }

    /**
     * Initialize constellation name mappings.
     */
    private void initializeNameMap() {
        nameMap = new HashMap<>();
        nameMap.put("And", "Andromeda");
        nameMap.put("Ant", "Antlia");
        nameMap.put("Aps", "Apus");
        nameMap.put("Aqr", "Aquarius");
        nameMap.put("Aql", "Aquila");
        nameMap.put("Ara", "Ara");
        nameMap.put("Ari", "Aries");
        nameMap.put("Aur", "Auriga");
        nameMap.put("Boo", "Boötes");
        nameMap.put("Cae", "Caelum");
        nameMap.put("Cam", "Camelopardalis");
        nameMap.put("Cnc", "Cancer");
        nameMap.put("CVn", "Canes Venatici");
        nameMap.put("CMa", "Canis Major");
        nameMap.put("CMi", "Canis Minor");
        nameMap.put("Cap", "Capricornus");
        nameMap.put("Car", "Carina");
        nameMap.put("Cas", "Cassiopeia");
        nameMap.put("Cen", "Centaurus");
        nameMap.put("Cep", "Cepheus");
        nameMap.put("Cet", "Cetus");
        nameMap.put("Cha", "Chamaeleon");
        nameMap.put("Cir", "Circinus");
        nameMap.put("Col", "Columba");
        nameMap.put("Com", "Coma Berenices");
        nameMap.put("CrA", "Corona Australis");
        nameMap.put("CrB", "Corona Borealis");
        nameMap.put("Crv", "Corvus");
        nameMap.put("Crt", "Crater");
        nameMap.put("Cru", "Crux");
        nameMap.put("Cyg", "Cygnus");
        nameMap.put("Del", "Delphinus");
        nameMap.put("Dor", "Dorado");
        nameMap.put("Dra", "Draco");
        nameMap.put("Equ", "Equuleus");
        nameMap.put("Eri", "Eridanus");
        nameMap.put("For", "Fornax");
        nameMap.put("Gem", "Gemini");
        nameMap.put("Gru", "Grus");
        nameMap.put("Her", "Hercules");
        nameMap.put("Hor", "Horologium");
        nameMap.put("Hya", "Hydra");
        nameMap.put("Hyi", "Hydrus");
        nameMap.put("Ind", "Indus");
        nameMap.put("Lac", "Lacerta");
        nameMap.put("Leo", "Leo");
        nameMap.put("LMi", "Leo Minor");
        nameMap.put("Lep", "Lepus");
        nameMap.put("Lib", "Libra");
        nameMap.put("Lup", "Lupus");
        nameMap.put("Lyn", "Lynx");
        nameMap.put("Lyr", "Lyra");
        nameMap.put("Men", "Mensa");
        nameMap.put("Mic", "Microscopium");
        nameMap.put("Mon", "Monoceros");
        nameMap.put("Mus", "Musca");
        nameMap.put("Nor", "Norma");
        nameMap.put("Oct", "Octans");
        nameMap.put("Oph", "Ophiuchus");
        nameMap.put("Ori", "Orion");
        nameMap.put("Pav", "Pavo");
        nameMap.put("Peg", "Pegasus");
        nameMap.put("Per", "Perseus");
        nameMap.put("Phe", "Phoenix");
        nameMap.put("Pic", "Pictor");
        nameMap.put("Psc", "Pisces");
        nameMap.put("PsA", "Piscis Austrinus");
        nameMap.put("Pup", "Puppis");
        nameMap.put("Pyx", "Pyxis");
        nameMap.put("Ret", "Reticulum");
        nameMap.put("Sge", "Sagitta");
        nameMap.put("Sgr", "Sagittarius");
        nameMap.put("Sco", "Scorpius");
        nameMap.put("Scl", "Sculptor");
        nameMap.put("Sct", "Scutum");
        nameMap.put("Ser", "Serpens");
        nameMap.put("Sex", "Sextans");
        nameMap.put("Tau", "Taurus");
        nameMap.put("Tel", "Telescopium");
        nameMap.put("Tri", "Triangulum");
        nameMap.put("TrA", "Triangulum Australe");
        nameMap.put("Tuc", "Tucana");
        nameMap.put("UMa", "Ursa Major");
        nameMap.put("UMi", "Ursa Minor");
        nameMap.put("Vel", "Vela");
        nameMap.put("Vir", "Virgo");
        nameMap.put("Vol", "Volans");
        nameMap.put("Vul", "Vulpecula");
    }
}
