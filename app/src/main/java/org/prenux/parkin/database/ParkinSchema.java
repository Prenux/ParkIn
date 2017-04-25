package org.prenux.parkin.database;



public class ParkinSchema {
    public static class ParkinTable {
        public static String NAME = "Reglementation";


        public static class Cols {
            public static final String ID = "id";
            public static final String Longtitude = "longtitude";
            public static final String Magnetude = "magnetude";
            public static final String LongtitudeC = "longtitudeC";
            public static final String MagnetudeC = "magnetudeC";
            public static final String Rue = "rue";
            public static final String Tarif = "tarif";

        }
    }

    public static class ParkinFree {
        public static String Name = "ReglementationF";

        public static class Cols {
            public static final String Longitutde = "longitutde";
            public static final String Latitude = "latitude";
            public static final String Description = "description";
            public static final String Code = "code";

        }
    }
}