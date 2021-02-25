package eci.arsw.covidanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A Camel Application
 */
public class CovidAnalyzerTool implements Runnable{

    private ResultAnalyzer resultAnalyzer;
    private TestReader testReader;
    private int amountOfFilesTotal;
    private AtomicInteger amountOfFilesProcessed;
    private ConcurrentLinkedDeque <ProcessThread> threads;
    private static final int ThreadNumber = 5;
    private boolean pause = false;

    public CovidAnalyzerTool() {
        resultAnalyzer = new ResultAnalyzer();
        testReader = new TestReader();
        amountOfFilesProcessed = new AtomicInteger();
        threads = new ConcurrentLinkedDeque<>();
        pause = false;
    }

    /**
     * Añade los threads segun el numero de threads que se le indico.
     */
    public void processResultData() {
        amountOfFilesProcessed.set(0);
        List<File> resultFiles = getResultFileList();
        amountOfFilesTotal = resultFiles.size();
        int range amountOfFilesTotal/ThreadNumber;
        for (int i = 0; i <= ThreadNumber-1; i++){
            if (i == ThreadNumber-1){
                threads.add(new ProcessThread(resultFiles, resultAnalyzer, amountOfFilesProcessed, testReader, i*range, amountOfFilesTotal-1));
            } else{
                threads.add(new ProcessThread(resultFiles, resultAnalyzer, amountOfFilesProcessed, testReader, i*range, (i*range)+range-1));
            }
            threads.getLast().start();
        }
        for (File resultFile : resultFiles) {
            List<Result> results = testReader.readResultsFromFile(resultFile);
            for (Result result : results) {
                resultAnalyzer.addResult(result);
            }
            amountOfFilesProcessed.incrementAndGet();
        }
    }

    private List<File> getResultFileList() {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }


    public Set<Result> getPositivePeople() {

        return resultAnalyzer.listOfPositivePeople();
    }

    /**
     *Vuelve a correr los threads que se iniciaron
     */
    public void resumeThread(){
        for (ProcessThread thread : threads){
            thread.resume();
        }
    }

    /**
     * Pausa los threads iniciados cambiando la variable pause a true
     */
    public void pauseThread(){
        pause = true;
        for (ProcessThread thread : threads){
            thread.pauseThread();
        }
        try{
            Thread.sleep(200);
        } catch (InterrumpedException e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo el cual pausa o continua los threads si se oprime una tecla
     */
    public void run(){
        Scanner scanner;
        Thread thread = new Thread(() -> processResultData());
        thread.start;

        while (amountOfFilesTotal == -1 || amountOfFilesProcessed.get()<amountOfFilesTotal){
            scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if (line.contains("exit")) {
                break;
            } else if (line.isEmpty()){
                if (pause){
                    resumeThread();
                }else {
                    pauseThread();
                }
            }else if (!pause && !line.isEmpty()){
                showMessage();
            }
        }
    }

    public void showMessage(){
        String message = "Processed %d out of %d files.\nFound %d positive people:\n%s";
        Set<Result> positivePeople = covidAnalyzerTool.getPositivePeople();
        String affectedPeople = positivePeople.stream().map(Result::toString).reduce("", (s1, s2) -> s1 + "\n" + s2);
        message = String.format(message, covidAnalyzerTool.amountOfFilesProcessed.get(), covidAnalyzerTool.amountOfFilesTotal, positivePeople.size(), affectedPeople);
        System.out.println(message);
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Thread thread = new Thread(new CovidAnalyzerTool());
        thread.start();
    }

}

