import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Test {
    private static final int ROWS = 0;
    private static final int COLS = 1;
    private static final int MAX_NUM = 2;
    private static final int VARS_NUM = 3;
    private static final int CLAUSES_NUM = 4;
    private static final int TIME = 5;
    private static final int SAT = 6;
    static Controller controller = new Controller();
    static String inputFolderPath1 = "./input/";
//    static String inputFolderPath2 = "E:\\Lab\\TC";
    public static File inFolder = new File(inputFolderPath1);
    public static File outFile = new File("./output/out3101.txt");

    static List<String> res;

    public static void listFilesForFolder(final File folder) throws InterruptedException, IOException, TimeoutException, ParseFormatException, ContradictionException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if (fileEntry.isFile()) {
                    String fileInfo = "";
                    String fileName = "";
                    fileName = fileEntry.getName();
                    if ((fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()).equals("in")) {
                        String time = "";
                        controller.read(fileEntry);
                        long t1 = System.currentTimeMillis();
                        ExecutorService executor = Executors.newFixedThreadPool(4);
                        Future<?> future = executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    controller.encode();
                                    //controller.write();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (TimeoutException e) {
                                    e.printStackTrace();
                                } catch (ParseFormatException e) {
                                    e.printStackTrace();
                                } catch (ContradictionException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        executor.shutdown();            //        reject all further submissions

                        try {
                            future.get(900, TimeUnit.SECONDS);  //     wait Time (seconds) to finish
                        } catch (InterruptedException e) {    //     possible error cases
                            System.out.println("job was interrupted");
                        } catch (ExecutionException e) {
                            System.out.println("caught exception: " + e.getCause());
                        } catch (java.util.concurrent.TimeoutException e) {
                            future.cancel(true);              //     interrupt the job
                            System.out.println("timeout");
                            controller.sat = "UNSAT";
                            System.out.println("UNSAT");
                            time = "time out";
                        }
                        // wait all unfinished tasks for sec
                        if(!executor.awaitTermination(1, TimeUnit.SECONDS)){
                            // force them to quit by interrupting
                            executor.shutdownNow();
                        }

                        res = controller.inFoList();
                        //System.out.println(res.get(TIME));
                        if (time != "time out") {
                            time = res.get(TIME);
                            System.out.println("\nTotal Time: " + time + " ms");
                        }

                        System.out.println("--------------------------------");
                        fileInfo += fileName + "\t" + res.get(ROWS) + "x" + res.get(COLS) + "\t" + res.get(MAX_NUM) + "\t"
                                + res.get(VARS_NUM) + "\t" + res.get(CLAUSES_NUM) + "\t" + time+ "\t" + res.get(SAT);
                    }
                    Controller.outputToTxt(fileInfo, outFile);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException, ParseFormatException, ContradictionException {
        //new Test(300);
        listFilesForFolder(inFolder);
        //reformatInput(reformatFolder);
    }
}