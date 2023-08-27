package sharan.experiments.tvsnavigator.utils

class Direction {
    companion object {
        fun getTVSPictogramAndDirection(gMapsDirection: String): Pair<Int, String> {
            return when (gMapsDirection) {
                "ARRIVE" -> Pair(8, "DESTINATION")
                "ARRIVE_LEFT" -> Pair(8, "DESTINATION:LEFT")
                "ARRIVE_RIGHT" -> Pair(8, "DESTINATION:RIGHT")
                "ARRIVE_STRAIGHT" -> Pair(8, "DESTINATION:STRAIGHT")
                "CONTINUE_STRAIGHT" -> Pair(7, "CONTINUE:STRAIGHT")
                "CONTINUE_LEFT" -> Pair(22, "CONTINUE:LEFT")
                "CONTINUE_RETURN" -> Pair(6, "MAKE A:U TURN")
                "CONTINUE_RIGHT" -> Pair(24, "CONTINUE:RIGHT")
                "CONTINUE_SLIGHT_LEFT", "SLIGHT_LEFT" -> Pair(2, "SLIGHT LEFT")
                "CONTINUE_SLIGHT_RIGHT", "SLIGHT_RIGHT" -> Pair(5, "SLIGHT RIGHT")
                "DEPART" -> Pair(7, "DEPART")
                "FORK" -> Pair(7, "FORK")
                "ROTATORY_EXIT", "ROTATORY_EXIT_INVERTED", "ROTATORY_STRAIGHT", "ROTATORY_STRAIGHT_INVERTED" -> Pair(68, "ROUNDABOUT: EXIT STRAIGHT")
                "ROTATORY_LEFT", "ROTATORY_LEFT_INVERTED" -> Pair(66, "ROUNDABOUT: EXIT LEFT")
                "ROTATORY_RIGHT", "ROTATORY_RIGHT_INVERTED" -> Pair(70, "ROUNDABOUT: EXIT RIGHT")
                "ROTATORY_SHARP_LEFT", "ROTATORY_SHARP_LEFT_INVERTED" -> Pair(65, "ROUNDABOUT: EXIT SHARP-LEFT")
                "ROTATORY_SHARP_RIGHT", "ROTATORY_SHARP_RIGHT_INVERTED" -> Pair(71, "ROUNDABOUT: EXIT SHARP-LEFT")
                "ROTATORY_SLIGHT_LEFT", "ROTATORY_SLIGHT_LEFT_INVERTED" -> Pair(67, "ROUNDABOUT: EXIT SLIGHT-LEFT")
                "ROTATORY_SLIGHT_RIGHT", "ROTATORY_SLIGHT_RIGHT_INVERTED" -> Pair(69, "ROUNDABOUT: EXIT SLIGHT-RIGHT")
                "ROTATORY_TOTAL", "ROTATORY_TOTAL_INVERTED" -> Pair(6, "ROUNDABOUT: EXIT TOTAL")
                "SHARP_LEFT" -> Pair(1, "SHARP:LEFT TURN")
                "SHARP_RIGHT" -> Pair(4, "SHARP:RIGHT TURN")
                else -> {
                    Pair(8, "UNKNOWN")
                }
            }
        }
    }
}