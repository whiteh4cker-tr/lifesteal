package tr.alperendemir.helix.api.players;

import java.util.TimeZone;

public interface PlayerInformation {

    String country();

    /**
     * @return if the presumed country that the user is in uses a month/day/year format
     * */
    boolean usesBackwardsDate();

    TimeZone timezone();

}
