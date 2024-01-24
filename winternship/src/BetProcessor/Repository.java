package BetProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Repository class manages the data for matches and players in a betting system.
 * It reads input from files, processes the data, and writes results to an output file.
 */
public class Repository {

    private final String MATCH_DATA_PATH = "resources/match_data.txt";
    private final String PLAYER_DATA_PATH = "resources/player_data.txt";
    private final String OUTPUT_PATH = "src/BetProcessor/result.txt";

    private final Map<UUID, Match> matches;
    private final Map<UUID, Player> players;

    /**
     * Constructs a Repository object and initializes match and player data.
     */
    public Repository() {
        matches = getMatches();
        players = getPlayers();
    }

    /**
     * Reads match data from the file declared as constant MATCH_DATA_PATH and returns a map of matches.
     *
     * @return A map containing match IDs as keys and Match objects as values.
     * @throws RuntimeException If the match data file is missing or cannot be read.
     */
    public Map<UUID, Match> getMatches() {
        List<String> lines;

        try {
            lines = Files.readAllLines(Paths.get(MATCH_DATA_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Match data file missing from resources folder", e);
        }

        Map<UUID, Match> matches = new HashMap<>();
        for (String line : lines) {
            String[] matchData = line.split(",");
            UUID matchId = UUID.fromString(matchData[0]);
            double rateA = Double.parseDouble(matchData[1]);
            double rateB = Double.parseDouble(matchData[2]);
            String result = matchData[3];

            Match match = new Match(matchId, rateA, rateB, result);
            matches.put(matchId, match);
        }

        return matches;
    }

    /**
     * Reads player data from the file declared as constant PLAYER_DATA_PATH and returns a map of players.
     *
     * @return A map containing player IDs as keys and Player objects as values.
     * @throws RuntimeException If the player data file is missing or cannot be read.
     */
    public Map<UUID, Player> getPlayers() {
        List<String> lines;

        try {
            lines = Files.readAllLines(Paths.get(PLAYER_DATA_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Player data file missing from resources folder", e);
        }

        Map<UUID, Player> players = new HashMap<>();
        for (String line : lines) {
            String[] playerOperation = line.split(",");
            UUID playerId = UUID.fromString(playerOperation[0]);
            String operationType = playerOperation[1];
            int coinAmount = Integer.parseInt(playerOperation[3]);
            if (coinAmount < 0) {
                throw new RuntimeException("Player operation coin amount is lesser than zero in " + line);
            }

            Player player;
            if (!players.containsKey(playerId)) {
                players.put(playerId, new Player(playerId));
            }
            player = players.get(playerId);
            if (!player.isLegitimate()) {
                continue;
            }

            switch (operationType) {
                case "DEPOSIT" -> player.changeBalance(coinAmount);
                case "WITHDRAW" -> {
                    if (player.getBalance() >= coinAmount) {
                        player.changeBalance(-coinAmount);
                    } else {
                        player.makeIllegitimate(line);
                    }
                }
                case "BET" -> {
                    if (player.getBalance() < coinAmount) {
                        player.makeIllegitimate(line);
                        continue;
                    }
                    UUID matchId = UUID.fromString(playerOperation[2]);
                    String betSide = playerOperation[4];
                    Match match;
                    if (!matches.containsKey(matchId)) {
                        throw new RuntimeException(String.format("Match %s not found!", matchId));
                    }
                    match = matches.get(matchId);
                    player.bet(match, betSide, coinAmount);
                }
            }
        }
        return players;
    }

    /**
     * Writes the result file containing information about legitimate players and illegitimate players if they exist and host balance change.
     * The result file is written to the output path constant declared as OUTPUT_PATH.
     *
     * @throws RuntimeException If an error occurs while writing the result file.
     */
    public void writeResultFile() {
        List<String> linesList = new ArrayList<>();

        List<Player> legitimatePlayers = players.values()
                .stream()
                .filter(p -> p.isLegitimate())
                .sorted(Comparator.comparing(player -> player.getPlayerId().toString()))
                .toList();

        List<Player> illegitimatePlayers = players.values()
                .stream()
                .filter(p -> !p.isLegitimate())
                .sorted(Comparator.comparing(player -> player.getPlayerId().toString()))
                .toList();

        if (legitimatePlayers.size() == 0) {
            linesList.add("");
        } else {
            for (Player player : legitimatePlayers) {
                String line = String.format("%s %s %s", player.getPlayerId().toString(), player.getBalance(), player.getWinRate().toString().replace(".", ","));
                linesList.add(line);
            }
        }

        linesList.add("");

        if (illegitimatePlayers.size() == 0) {
            linesList.add("");
        } else {
            for (Player player : illegitimatePlayers) {
                linesList.add(player.getFirstIllegalOperation());
            }
        }

        linesList.add("");

        long hostBalanceChange = -legitimatePlayers.stream()
                .mapToLong(player -> player.getTotalBetReturns())
                .sum();
        linesList.add(String.valueOf(hostBalanceChange));

        String lines = String.join("\n", linesList);

        try {
            Files.write(Paths.get(OUTPUT_PATH), lines.getBytes());
            System.out.println("File has been written successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Error writing result file", e);
        }
    }
}
