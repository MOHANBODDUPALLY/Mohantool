package rates_upd;

public class Main {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("        MERCURY C_TEMP_DATA UPDATER");
        System.out.println("==============================================");

        if (args.length < 2) {
            System.out.println();
            System.out.println("Usage:");
            System.out.println(
                "java -jar Rates_Update_preprod.jar <C_MAIN_REF> <PROCESS_NAME>"
            );
            System.out.println();
            System.out.println("Example:");
            System.out.println(
                "java -jar Rates_Update_preprod.jar 0999525US0B00270 update_ILCBILLPYMT"
            );
            System.out.println();
            System.out.println("For 3 minute execution:");
            System.out.println(
                "java -jar Rates_Update_preprod.jar 0999525US0B00270 update_ILCBILLPYMT --loop"
            );
            return;
        }

        String cMainRef = args[0];
        String processName = args[1];

        boolean loop = false;

        if (args.length >= 3 &&
            "--loop".equalsIgnoreCase(args[2])) {

            loop = true;
        }

        try {

            RateUpdateService service =
                new RateUpdateService();

            if (loop) {

                System.out.println(
                    "3 minute automatic mode enabled."
                );

                while (true) {

                    try {

                        service.process(
                            cMainRef,
                            processName
                        );

                    } catch (Exception e) {

                        System.err.println(
                            "Process failed: " + e.getMessage()
                        );

                        e.printStackTrace();
                    }

                    System.out.println();
                    System.out.println(
                        "Waiting 3 minutes for next execution..."
                    );

                    Thread.sleep(180000);

                }

            } else {

                service.process(
                    cMainRef,
                    processName
                );
            }

        } catch (Exception e) {

            System.err.println(
                "Application failed: " + e.getMessage()
            );

            e.printStackTrace();

            System.exit(1);
        }
    }
}