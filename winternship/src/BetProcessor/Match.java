package BetProcessor;

import java.util.UUID;

public class Match {

    private final UUID matchId;
    private final double aReturn;
    private final double bReturn;
    private final String winner;

    public static final String SIDE_A = "A";
    public static final String SIDE_B = "B";
    public static final String DRAW = "DRAW";

    public Match(UUID matchId, double aReturn, double bReturn, String winner) {
        this.matchId = matchId;
        this.aReturn = aReturn;
        this.bReturn = bReturn;
        this.winner = winner;
    }

    /**
     * Gets the ID of the match.
     *
     * @return The match ID.
     */
    public UUID getMatchId() {
        return matchId;
    }

    /**
     * Gets the winner of the match.
     *
     * @return The match winner ("A," "B," or "DRAW").
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Gets the return for the specified side in the match.
     *
     * @param side The side ("A" or "B").
     * @return The return for the specified side.
     */
    public double getPlayerReturn(String side) {
        if (DRAW.equals(winner)) return 1;

        if (SIDE_A.equals(side)) return aReturn;
        else if (SIDE_B.equals(side)) return bReturn;
        else throw new IllegalArgumentException("Invalid side: " + side);
    }
}
