package org.prenux.parkin.database;



public class ParkinSchema {
    public static class Parcometer {
        public static String NAME = "PARCOMETER";


        public static class Cols {
            public static final String ID = "id";
            public static final String LONGITUDE = "longitude";
            public static final String MAGNITUDE = "magnitude";
            public static final String TARIF = "tarif";

        }
    }


    public static class ParkinFree {
        public static String NAME = "PARKINFREE";

        public static class Cols {
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
            public static final String DESCRIPTION = "description";
            public static final String CODE = "code";

        }
    }
}