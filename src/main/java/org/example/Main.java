package org.example;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;


public class Main {
    private static final DbSettings SETTINGS = new DbSettings();
    private static final String SELECT_FROM_DB = "SELECT * FROM %s";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS %s";

    private static final String COORDINATES_TABLE = "Coordinates";
    private static final String FREQUENCIES_TABLE = "Frequencies";
    private static final String CREATE_TABLE = "CREATE TABLE %s ( id INTEGER AUTO_INCREMENT PRIMARY KEY, len INT, num INT );";
    private static final String INSERT_INTO = "INSERT INTO %s (len, num) VALUES (?, ?)";
    private static final String SELECT_FROM_WHERE_LEN_BIGGER_THAN_NUM = "SELECT * FROM %s WHERE len>num";

    private static final String SEPARATOR = ":";
    private static final int LEN_IDX = 1;
    private static final int NUM_IDX = 2;

    public static void main(String[] args) {
        class Pair {
            final int len;
            int num;

            Pair(int len, int num) {
                this.len = len;
                this.num = num;
            }

            @Override
            public String toString() {
                return len + ":" + num;
            }
        }

        final var frequencies = new ArrayList<Pair>();

        try {
            try (
                    final var cn = DriverManager.getConnection(SETTINGS.getDbUrl(), SETTINGS.getUser(), SETTINGS.getPassword());
                    final var st = cn.createStatement();
            ) {
                try (final var rs = st.executeQuery(String.format(SELECT_FROM_DB, COORDINATES_TABLE))) {
                    while (rs.next()) {
                        final var x1 = rs.getDouble(2);
                        final var x2 = rs.getDouble(3);

                        final var len = (int) (Math.abs(Math.round(x1) - Math.round(x2)));

                        var wasFound = false;
                        for (final var freq : frequencies) {
                            if (freq.len == len) {
                                freq.num += 1;
                                wasFound = true;
                                break;
                            }
                        }

                        if (!wasFound) {
                            frequencies.add(new Pair(len, 1));
                        }
                    }

                    frequencies.sort(Comparator.comparingInt(o -> o.len));

                    st.execute(String.format(DROP_TABLE, FREQUENCIES_TABLE));
                    st.execute(String.format(CREATE_TABLE, FREQUENCIES_TABLE));

                    final var statement = cn.prepareStatement(String.format(INSERT_INTO, FREQUENCIES_TABLE));
                    for (final var freq : frequencies) {
                        System.out.println(freq.len + ":" + freq.num);

                        statement.setInt(LEN_IDX, freq.len);
                        statement.setInt(NUM_IDX, freq.num);

                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                try (final var rs = st.executeQuery(String.format(SELECT_FROM_WHERE_LEN_BIGGER_THAN_NUM, FREQUENCIES_TABLE))) {
                    System.out.println();
                    while (rs.next()) {
                        System.out.println(rs.getInt(2) + SEPARATOR + rs.getInt(3));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}