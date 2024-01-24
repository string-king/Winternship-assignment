package BetProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * The {@code Player} class represents a player in a betting system.
 * It keeps track of the player's balance, bets, and provides methods
 * for betting, checking the win rate, and handling illegitimate operations.
 */
public class Player {

    /** The unique identifier for the player. */
    private final UUID playerId;

    /** The current balance of the player. */
    private long balance;

    /** The total number of bets placed by the player. */
    private int totalBets;

    /** The number of bets won by the player. */
    private int betsWon;

    /** Indicates whether the player is legitimate or not. */
    private boolean isLegitimate;

    /** The first illegal operation encountered by the player. */
    private String firstIllegalOperation;

    /** The total returns from all bets placed by the player. */
    private long totalBetReturns;

    /** The set to store match UUID-s that player has bet on. */
    private Set<UUID> matchesBet;

    /**
     * Constructs a new player with the given unique identifier.
     *
     * @param playerId The unique identifier for the player.
     */
    public Player(UUID playerId) {
        this.playerId = playerId;
        this.balance = 0;
        this.totalBets = 0;
        this.betsWon = 0;
        this.isLegitimate = true;
        matchesBet = new HashSet<>();
        this.totalBetReturns = 0;
    }

    /**
     * Gets the unique identifier of the player.
     *
     * @return The unique identifier of the player.
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Gets the current balance of the player.
     *
     * @return The current balance of the player.
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Checks if the player is legitimate.
     *
     * @return {@code true} if the player is legitimate, {@code false} otherwise.
     */
    public boolean isLegitimate() {
        return isLegitimate;
    }

    /**
     * Changes the balance of the player by the specified amount.
     *
     * @param change The amount to change the balance.
     */
    public void changeBalance(int change) {
        balance += change;
    }

    /**
     * Marks the player as illegitimate and records the first illegal operation encountered.
     *
     * @param illegalOperation The first illegal operation encountered.
     */
    public void makeIllegitimate(String illegalOperation) {
        isLegitimate = false;
        firstIllegalOperation = getIllegitimateOperationString(illegalOperation);
    }

    /**
     * Gets the win rate of the player.
     *
     * @return The win rate of the player as a {@code BigDecimal}.
     */
    public BigDecimal getWinRate() {
        if (totalBets == 0) {
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(betsWon).divide(new BigDecimal(totalBets), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Places a bet for the player on a given match and side.
     * If the player attempts to bet on a match multiple times, an exception is thrown.
     *
     * @param match      The match to bet on.
     * @param side       The side to bet on.
     * @param coinAmount The amount of coins to bet.
     * @throws RuntimeException If the player tried to bet the same match multiple times.
     */
    public void bet(Match match, String side, int coinAmount) {
        if (matchesBet.contains(match.getMatchId())) {
            throw new RuntimeException(String.format("Player %s tried to bet multiple times on match %s",
                    playerId.toString(),
                    match.getMatchId().toString()));
        }
        totalBets++;
        matchesBet.add(match.getMatchId());
        if (match.getWinner().equals(side)) {
            betsWon++;
            int betReturn = (int) Math.floor(coinAmount * match.getPlayerReturn(side));
            balance += betReturn;
            totalBetReturns += betReturn;
        } else if (!match.getWinner().equals("DRAW")) {
            balance -= coinAmount;
            totalBetReturns -= coinAmount;
        }
    }

    /**
     * Formats the details of an illegitimate operation into a string.
     *
     * @param line The illegitimate operation details.
     * @return A formatted string representing the illegitimate operation for output file.
     */
    private String getIllegitimateOperationString(String line) {
        String[] operation = line.split(",");
        String playerId = operation[0];
        String operationType = operation[1];
        String matchId = operation[2];
        String coinAmount = operation[3];
        String betSide;

        if (matchId.isBlank()) {
            matchId = betSide = "null";
        } else {
            betSide = operation[4];
        }

        return String.format("%s %s %s %s %s", playerId, operationType, matchId, coinAmount, betSide);
    }

    /**
     * Gets the details of the first illegal operation encountered by the player.
     *
     * @return The details of the first illegal operation.
     */
    public String getFirstIllegalOperation() {
        return firstIllegalOperation;
    }

    /**
     * Gets the total returns from all bets placed by the player.
     *
     * @return The total returns from all bets.
     */
    public long getTotalBetReturns() {
        return totalBetReturns;
    }
}
