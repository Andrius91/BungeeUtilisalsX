package com.dbsoftwares.bungeeutilisals.storage.data;

/*
 * Created by DBSoftwares on 18/01/2018
 * Developer: Dieter Blancke
 * Project: BungeeUtilisals
 */

import com.dbsoftwares.bungeeutilisals.BungeeUtilisals;
import com.dbsoftwares.bungeeutilisals.api.BUCore;
import com.dbsoftwares.bungeeutilisals.api.language.Language;
import com.dbsoftwares.bungeeutilisals.api.placeholder.PlaceHolderAPI;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentInfo;
import com.dbsoftwares.bungeeutilisals.api.punishments.PunishmentType;
import com.dbsoftwares.bungeeutilisals.api.storage.DataManager;
import com.dbsoftwares.bungeeutilisals.api.user.UserStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SQLDataManager implements DataManager {

    /* SQL INSERT STATEMENTS */
    private final static String INSERT_INTO_USERS = "INSERT INTO {users-table} (uuid, username, ip, language) VALUES ('%s', '%s', '%s', '%s');";

    private final static String INSERT_INTO_BANS = "INSERT INTO {bans-table} (uuid, user, ip, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_IPBANS = "INSERT INTO {ipbans-table} (uuid, user, ip, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_TEMPBANS = "INSERT INTO {tempbans-table} (uuid, user, ip, time, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', %s, '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_IPTEMPBANS = "INSERT INTO {iptempbans-table} (uuid, user, ip, time, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', %s, '%s', '%s', %s, '%s');";

    private final static String INSERT_INTO_MUTES = "INSERT INTO {mutes-table} (uuid, user, ip, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_IPMUTES = "INSERT INTO {ipmutes-table} (uuid, user, ip, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_TEMPMUTES = "INSERT INTO {tempmutes-table} (uuid, user, ip, time, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', %s, '%s', '%s', %s, '%s');";
    private final static String INSERT_INTO_IPTEMPMUTES = "INSERT INTO {iptempmutes-table} (uuid, user, ip, time, reason, server, active, executed_by) VALUES ('%s', '%s', '%s', %s, '%s', '%s', %s, '%s');";

    private final static String INSERT_INTO_WARNS = "INSERT INTO {warns-table} (uuid, user, ip, reason, server, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');";
    private final static String INSERT_INTO_KICKS = "INSERT INTO {kicks-table} (uuid, user, ip, reason, server, executed_by) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');";

    /* SQL SELECT STATEMENT */
    private final static String SELECT = "SELECT %s FROM {table} WHERE %s;";
    private final static String SELECT_ORDER = "SELECT %s FROM {table} WHERE %s ORDER BY %s;";

    private final static String SELECT_FROM_USERS = SELECT.replace("{table}", "{users-table}");
    private final static String SELECT_USERS_ON_IP = SELECT_ORDER.replace("{table}", "{users-table}");

    private final static String SELECT_FROM_BANS = SELECT.replace("{table}", "{bans-table}");
    private final static String SELECT_FROM_IPBANS = SELECT.replace("{table}", "{ipbans-table}");
    private final static String SELECT_FROM_TEMPBANS = SELECT.replace("{table}", "{tempbans-table}");
    private final static String SELECT_FROM_IPTEMPBANS = SELECT.replace("{table}", "{iptempbans-table}");

    private final static String SELECT_FROM_MUTES = SELECT.replace("{table}", "{mutes-table}");
    private final static String SELECT_FROM_IPMUTES = SELECT.replace("{table}", "{ipmutes-table}");
    private final static String SELECT_FROM_TEMPMUTES = SELECT.replace("{table}", "{tempmutes-table}");
    private final static String SELECT_FROM_IPTEMPMUTES = SELECT.replace("{table}", "{iptempmutes-table}");

    private final static String SELECT_FROM_WARNS = SELECT.replace("{table}", "{warns-table}");
    private final static String SELECT_FROM_KICKS = SELECT.replace("{table}", "{kicks-table}");

    /* SQL DELETE STATEMENTS */
    private final static String UPDATE_PUNISHMENTS_UUID = "UPDATE {table} SET active = 0 WHERE uuid = '%s' AND active = 1;";
    private final static String UPDATE_PUNISHMENTS_IP = "UPDATE {table} SET active = 0 WHERE ip = '%s' AND active = 1;";

    /* UTILITY METHODS */
    private static Language getLanguageOrDefault(String language) {
        return BUCore.getApi().getLanguageManager().getLanguage(language).orElse(BUCore.getApi().getLanguageManager().getDefaultLanguage());
    }



    @Override
    public long getPunishmentsSince(String identifier, PunishmentType type, Date date) {
        long amount = 0;

        String statement = format(SELECT.replace("{table}", type.getTablePlaceHolder()), "COUNT(*) count",
                (type.toString().startsWith("IP") ? "ip" : "uuid") + " = '" + identifier + "' AND date >= ?;");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setDate(1, new java.sql.Date(date.getTime()));

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                amount = resultSet.getLong("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }

    /* INSERTION STATEMENTS */
    @Override
    public void insertIntoUsers(String uuid, String username, String ip, String language) {
        String statement = format(INSERT_INTO_USERS, uuid, username, ip, language);

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PunishmentInfo insertPunishment(PunishmentType type, UUID uuid, String user,
                                           String ip, String reason, Long time, String server,
                                           Boolean active, String executedby) {
        String sql = "INSERT INTO " + PlaceHolderAPI.formatMessage(type.getTablePlaceHolder()) + " ";

        if (type.isActivatable()) {
            if (type.isTemporary()) {
                sql += "(uuid, user, ip, time, reason, server, active, executed_by) "
                        + "VALUES ('%s', '%s', '%s', %s, '%s', '%s', %s, '%s');";

                try (Connection connection = BUCore.getApi().getStorageManager().getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setString(2, user);
                    preparedStatement.setString(3, ip);
                    preparedStatement.setLong(4, time);
                    preparedStatement.setString(5, reason);
                    preparedStatement.setString(6, server);
                    preparedStatement.setBoolean(7, active);
                    preparedStatement.setString(8, executedby);

                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                sql += "(uuid, user, ip, reason, server, active, executed_by) "
                        + "VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s');";

                try (Connection connection = BUCore.getApi().getStorageManager().getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setString(2, user);
                    preparedStatement.setString(3, ip);
                    preparedStatement.setString(4, reason);
                    preparedStatement.setString(5, server);
                    preparedStatement.setBoolean(6, active);
                    preparedStatement.setString(7, executedby);

                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sql += "(uuid, user, ip, reason, server, executed_by) "
                    + "VALUES ('%s', '%s', '%s', '%s', '%s', '%s');";

            try (Connection connection = BUCore.getApi().getStorageManager().getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, user);
                preparedStatement.setString(3, ip);
                preparedStatement.setString(4, reason);
                preparedStatement.setString(5, server);
                preparedStatement.setString(7, executedby);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /* UPDATE STATEMENTS */
    @Override
    public void updateUser(String uuid, String name, String ip, String language) {
        StringBuilder statement = new StringBuilder("UPDATE " + PlaceHolderAPI.formatMessage("{users-table}") + " SET ");

        if (name != null) {
            statement.append(" username = '").append(name).append("', ");
        }
        if (ip != null) {
            statement.append(" ip = '").append(ip).append("', ");
        }
        if (language != null) {
            statement.append(" language = '").append(language).append("', ");
        }
        statement.delete(statement.length() - 2, statement.length());
        statement.append(" WHERE uuid = '").append(uuid).append("';");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* SELECTION STATEMENTS */
    @Override
    public boolean isUserPresent(String name) {
        boolean present = false;
        String statement = format(SELECT_FROM_USERS, "id", (name.contains(".") ? "ip = '" : "username = '") + name + "'");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isUserPresent(UUID uuid) {
        boolean present = false;
        String statement = format(SELECT_FROM_USERS, "id", "uuid = '" + uuid + "'");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isPunishmentPresent(PunishmentType type, UUID uuid, String IP, boolean checkActive) {
        return false;
    }

    @Override
    public boolean isBanPresent(UUID uuid, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_BANS, "id", "uuid = '" + uuid + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isIPBanPresent(String ip, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_IPBANS, "id", "ip = '" + ip + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isTempBanPresent(UUID uuid, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_TEMPBANS, "id", "uuid = '" + uuid + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isIPTempBanPresent(String IP, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_IPTEMPBANS, "id", "ip = '" + IP + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isMutePresent(UUID uuid, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_MUTES, "id", "uuid = '" + uuid + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isIPMutePresent(String ip, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_IPMUTES, "id", "ip = '" + ip + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isTempMutePresent(UUID uuid, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_TEMPMUTES, "id", "uuid = '" + uuid + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public boolean isIPTempMutePresent(String IP, boolean checkActive) {
        boolean present = false;
        String statement = format(SELECT_FROM_IPTEMPMUTES, "id", "ip = '" + IP + "'" + (checkActive ? " AND active = 1" : ""));

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            present = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return present;
    }

    @Override
    public UserStorage getUser(UUID uuid) {
        UserStorage storage = new UserStorage();
        String statement = format(SELECT_FROM_USERS, "*", "uuid = '" + uuid + "'");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                storage.setUuid(uuid);
                storage.setUserName(resultSet.getString("username"));
                storage.setIp(resultSet.getString("ip"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storage;
    }

    @Override
    public Language getLanguage(UUID uuid) {
        Language language = null;
        String statement = format(SELECT_FROM_USERS, "language", "uuid = '" + uuid + "'");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                language = getLanguageOrDefault(resultSet.getString("language"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return language;
    }

    @Override
    public UserStorage getUser(String name) {
        UserStorage storage = new UserStorage();
        String statement = format(SELECT_FROM_USERS, "*", (name.contains(".") ? "ip = '" : "username = '") + name + "'");

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                storage.setUuid(UUID.fromString(resultSet.getString("uuid")));
                storage.setUserName(resultSet.getString("username"));
                storage.setIp(resultSet.getString("ip"));
                storage.setLanguage(getLanguageOrDefault(resultSet.getString("language")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return storage;
    }

    @Override
    public List<String> getUsersOnIP(String name) {
//        private final static String SELECT_ORDER = "SELECT %s FROM {table} WHERE %s ORDER BY %s;";
        format(SELECT_USERS_ON_IP, "username", "ip = ?", "lastjoined DESC");
        return null;
    }

    @Override
    public PunishmentInfo getPunishment(PunishmentType type, UUID uuid, String IP) {
        return null;
    }

    @Override
    public void removePunishment(PunishmentType type, UUID uuid, String IP) {

    }

    @Override
    public PunishmentInfo getBan(UUID uuid) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.BAN);

        String statement = format(SELECT_FROM_BANS, "*", "uuid = '" + uuid + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getIPBan(String IP) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.IPBAN);

        String statement = format(SELECT_FROM_IPBANS, "*", "ip = '" + IP + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getTempBan(UUID uuid) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.TEMPBAN);

        String statement = format(SELECT_FROM_TEMPBANS, "*", "uuid = '" + uuid + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setExpireTime(resultSet.getLong("time"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getIPTempBan(String IP) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.IPTEMPBAN);

        String statement = format(SELECT_FROM_IPTEMPBANS, "*", "ip = '" + IP + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setExpireTime(resultSet.getLong("time"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getMute(UUID uuid) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.MUTE);

        String statement = format(SELECT_FROM_MUTES, "*", "uuid = '" + uuid + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getIPMute(String IP) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.IPMUTE);

        String statement = format(SELECT_FROM_IPMUTES, "*", "ip = '" + IP + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getTempMute(UUID uuid) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.TEMPMUTE);

        String statement = format(SELECT_FROM_TEMPMUTES, "*", "uuid = '" + uuid + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setExpireTime(resultSet.getLong("time"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public PunishmentInfo getIPTempMute(String IP) {
        PunishmentInfo info = PunishmentInfo.builder().build();
        info.setType(PunishmentType.IPTEMPMUTE);

        String statement = format(SELECT_FROM_IPTEMPMUTES, "*", "ip = '" + IP + "' AND active = 1");
        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(statement);

            if (resultSet.next()) {
                info.setId(resultSet.getInt("id"));
                info.setUuid(UUID.fromString(resultSet.getString("uuid")));
                info.setUser(resultSet.getString("user"));
                info.setIP(resultSet.getString("ip"));
                info.setExpireTime(resultSet.getLong("time"));
                info.setReason(resultSet.getString("reason"));
                info.setServer(resultSet.getString("server"));
                info.setDate(resultSet.getTimestamp("date"));
                info.setActive(resultSet.getBoolean("active"));
                info.setExecutedBy(resultSet.getString("executed_by"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return info;
    }

    @Override
    public void removeBan(UUID uuid) {
        String statement = format(UPDATE_PUNISHMENTS_UUID.replace("{table}", "{bans-table}"), uuid.toString());

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeIPBan(String IP) {
        String statement = format(UPDATE_PUNISHMENTS_IP.replace("{table}", "{ipbans-table}"), IP);

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTempBan(UUID uuid) {
        String statement = format(UPDATE_PUNISHMENTS_UUID.replace("{table}", "{tempbans-table}"), uuid.toString());

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeIPTempBan(String IP) {
        String statement = format(UPDATE_PUNISHMENTS_IP.replace("{table}", "{iptempbans-table}"), IP);

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMute(UUID uuid) {
        String statement = format(UPDATE_PUNISHMENTS_UUID.replace("{table}", "{mutes-table}"), uuid.toString());

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeIPMute(String IP) {
        String statement = format(UPDATE_PUNISHMENTS_IP.replace("{table}", "{ipmutes-table}"), IP);

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeTempMute(UUID uuid) {
        String statement = format(UPDATE_PUNISHMENTS_UUID.replace("{table}", "{tempmutes-table}"), uuid.toString());

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeIPTempMute(String IP) {
        String statement = format(UPDATE_PUNISHMENTS_IP.replace("{table}", "{iptempmutes-table}"), IP);

        try (Connection connection = BungeeUtilisals.getInstance().getDatabaseManagement().getConnection()) {
            connection.createStatement().executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}