package me.davidml16.aparkour.database.types;

import me.davidml16.aparkour.Main;
import me.davidml16.aparkour.data.LeaderboardEntry;
import me.davidml16.aparkour.managers.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLite implements Database {

    private Connection connection;

    private Main main;

    public SQLite(Main main) {
        this.main = main;
    }

    @Override
    public void close() {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    public void open() {
        if (connection != null)  return;

        File file = new File(main.getDataFolder(), "playerData.db");
        String URL = "jdbc:sqlite:" + file;

        synchronized (this) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                Main.log.sendMessage(ColorManager.translate("    &aSQLite has been enabled!"));
            } catch (SQLException | ClassNotFoundException e) {
                Main.log.sendMessage(ColorManager.translate("    &cSQLite has an error on the conection! Plugin disabled : Database needed"));
                Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("AParkour"));
            }
        }
    }

    public void loadTables() {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ap_times (`UUID` varchar(40) NOT NULL, `parkourID` varchar(25) NOT NULL, `lastTime` bigint NOT NULL, `bestTime` bigint NOT NULL, PRIMARY KEY (`UUID`, `parkourID`));");
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        PreparedStatement statement2 = null;
        try {
            statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ap_playernames (`UUID` varchar(40) NOT NULL, `NAME` varchar(40), PRIMARY KEY (`UUID`));");
            statement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement2 != null) {
                try {
                    statement2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasData(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return false;
    }

    public void createData(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO ap_times (UUID,parkourID,lastTime,bestTime) VALUES(?,?,0,0)");
            ps.setString(1, uuid.toString());
            ps.setString(2, parkour);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
        }
    }

    public boolean hasName(Player p) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE UUID = '" + p.getUniqueId().toString() + "';");
            rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return false;
    }

    public void updatePlayerName(Player p) throws SQLException {
        PreparedStatement ps = null;
        try {
            if (!hasName(p)) {
                ps = connection.prepareStatement("INSERT INTO ap_playernames (UUID,NAME) VALUES(?,?)");
                ps.setString(1, p.getUniqueId().toString());
                ps.setString(2, p.getName());
            } else {
                ps = connection.prepareStatement("REPLACE INTO ap_playernames (UUID,NAME) VALUES(?,?)");
                ps.setString(1, p.getUniqueId().toString());
                ps.setString(2, p.getName());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
        }
    }

    public String getPlayerName(String uuid) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = connection.prepareStatement("SELECT * FROM ap_playernames WHERE UUID = '" + uuid + "';");
        rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getString("NAME");
        }

        return "";
    }

    public Long getLastTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("lastTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return 0L;
    }

    public Long getBestTime(UUID uuid, String parkour) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT * FROM ap_times WHERE UUID = '" + uuid.toString() + "' AND parkourID = '" + parkour + "';");

            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getLong("bestTime");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
            if(rs != null) rs.close();
        }

        return 0L;
    }

    public void setTimes(UUID uuid, Long lastTime, Long bestTime, String parkour) throws SQLException {
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("REPLACE INTO ap_times (UUID,parkourID,lastTime,bestTime) VALUES(?,?,?,?)");
            ps.setString(1, uuid.toString());
            ps.setString(2, parkour);
            ps.setLong(3, lastTime);
            ps.setLong(4, bestTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(ps != null) ps.close();
        }
    }

    public HashMap<String, Long> getPlayerLastTimes(UUID uuid) {
        HashMap<String, Long> times = new HashMap<String, Long>();
        for (String parkour : main.getParkourHandler().getParkours().keySet()) {
            try {
                if (hasData(uuid, parkour)) {
                    times.put(parkour, getLastTime(uuid, parkour));
                } else {
                    createData(uuid, parkour);
                    times.put(parkour, 0L);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return times;
    }

    public HashMap<String, Long> getPlayerBestTimes(UUID uuid) {
        HashMap<String, Long> times = new HashMap<String, Long>();
        for (String parkour : main.getParkourHandler().getParkours().keySet()) {
            try {
                if (hasData(uuid, parkour)) {
                    times.put(parkour, getBestTime(uuid, parkour));
                } else {
                    createData(uuid, parkour);
                    times.put(parkour, 0L);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return times;
    }

    public CompletableFuture<List<LeaderboardEntry>> getParkourBestTimes(String id, int amount) {
        CompletableFuture<List<LeaderboardEntry>> result = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List<LeaderboardEntry> times = new ArrayList<>();
            try {
                ps = connection.prepareStatement("SELECT * FROM ap_times WHERE bestTime != 0 AND parkourID = '" + id + "' ORDER BY bestTime ASC LIMIT " + amount + ";");

                rs = ps.executeQuery();
                while (rs.next()) {
                    times.add(new LeaderboardEntry(getPlayerName(rs.getString("UUID")), rs.getLong("bestTime")));
                }

                result.complete(times);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if(ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if(rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return result;
    }

}
