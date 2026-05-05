package tr.alperendemir.helix.players;

import tr.alperendemir.helix.api.players.PlayerInformation;

import java.util.TimeZone;

public record BukkitHelixPlayerInformation(String country, TimeZone timezone) implements PlayerInformation {

    @Override
    public boolean usesBackwardsDate() {
        return (this.country().equals("US") || this.country().equals("USA"))      // United States
                || (this.country().equals("BZ") || this.country().equals("BLZ"))  // Belize
                || (this.country().equals("FM") || this.country().equals("FSM")); // Micronesia
    }

}
