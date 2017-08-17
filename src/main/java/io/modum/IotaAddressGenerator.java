package io.modum;

import jota.error.InvalidAddressException;
import jota.pow.Kerl;
import jota.utils.IotaAPIUtils;
import jota.utils.StopWatch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;

public class IotaAddressGenerator {
    public static void main(String[] args) throws InvalidAddressException, IOException {
        final int SECURITY_LEVEL = 3;
        final String usage = "java -jar iota-keys.jar <81 length seed> <no of addresses>";
        if (args.length < 1) {
            System.err.println("Please specify Seed: " + usage);
            System.exit(1);
        }
        final String seed = args[0];
        if (!seed.matches("[A-Z9]{81}")) {
            System.err.println("Seed must be 81 characters long and only contain uppercase letters and '9'");
            System.exit(1);
        }
        if (args.length < 2) {
            System.err.println("Please specify how many addresses to generate: " + usage);
            System.exit(1);
        }
        int addressCount = 0;
        try {
            addressCount = Integer.valueOf(args[1]);
            if (addressCount < 0) throw new NumberFormatException();
        } catch(NumberFormatException e) {
            System.err.println("Invalid number of addresses: " + usage);
            System.exit(1);
        }

        StopWatch stopWatch = new StopWatch();

        try(BufferedWriter csv = Files.newBufferedWriter(Paths.get("iota-addresses.csv"))) {
            IntStream.range(0, addressCount).boxed().parallel()
                    .map(i -> {
                        try {
                            final String address = IotaAPIUtils.newAddress(seed, SECURITY_LEVEL, i, true, new Kerl());
                            return new Address(i, address);
                        } catch (InvalidAddressException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .forEach(address -> {
                        try {
                            csv.write(String.format("%d,%s\n", address.index, address.address));
                        } catch (IOException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });
        }

        System.out.println(String.format("Runtime: %d seconds", stopWatch.getElapsedTimeSecs()));
    }

    private static class Address {
        final Integer index;
        final String address;

        Address(int index, String address) {
            this.index = index;
            this.address = address;
        }
    }
}
