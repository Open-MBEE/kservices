package gov.nasa.jpl.kservices;

//import java.util.HashMap;
//import java.util.Map;

public class GNC {

    public static enum TARGET { X, Y, Z };
    static int[][] slewTimes = new int[][] {{0, 90, 100}, {60, 0, 80}, {50, 40, 0} };

    public static int slew(TARGET from, TARGET to) {
        return slewTimes[ from.ordinal() ][ to.ordinal() ];
    }
    public static int slew(int from, int to) {
        return slewTimes[ from ][ to ];
    }
}

//    {
//        switch (from) {
//            case X:
//                switch (to) {
//                    case Y:
//                        return 100;
//                    case Z:
//                        return 90;
//                    default:
//                        ;
//                }
//                break;
//            case Y:
//                switch (to) {
//                    case X:
//                        return 80;
//                    case Z:
//                        return 90;
//                    default:
//                        ;
//                }
//                break;
//            case Z:
//                switch (to) {
//                    case X:
//                        return 80;
//                    case Y:
//                        return 60;
//                    default:
//                        ;
//                }
//                break;
//        };
//        return 0;
//    }
//        //    Map<TARGET, Map<TARGET, Long>> slewTimes = new HashMap<TARGET, Map<TARGET, Long>>() {
//        {
//            add
//        }
//    };
//    public static slew(String from, String to) {
//        if ( from.equals( "TARGET_X" ) ) {
//        } else if ( from.equals( "TARGET_X" ) ) {
//        } else if ( from.equals( "TARGET_X" ) ) {
//    }
