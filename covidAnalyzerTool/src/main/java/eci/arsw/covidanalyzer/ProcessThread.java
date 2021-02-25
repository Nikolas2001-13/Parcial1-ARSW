package eci.arsw.covidanalyzer;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessThread extends Thread{

    private List<File> resultFiles;
    private ResultAnalyzer resultAnalyzer;
    private AtomicInteger amountOfFilesProcessed;
    private TestReader testReader;
    private boolean pause;
    private int x;
    private int y;

    public ProcessThread(List<Files> resultFiles, ResultAnalyzer resultAnalyzer, AtomicInteger amountOfFilesProcessed, TestReader testReader, int x, int y){
        this.resultFiles = resultFiles;
        this.resultAnalyzer = resultAnalyzer;
        this.amountOfFilesProcessed = amountOfFilesProcessed;
        this.testReader = testReader;
        this.pause = false;
        this.x = x;
        this.y = y;
    }

    public void run(){
        for (File file : resultFiles){
            synchronized (this) {
                while (pause){
                    try{
                        wait();
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }

            List<Result> results = testReader.readResultsFromFile(file);

            for (Result result : results){
                resultAnalyzer.addResult(result);
            }
            amountOfFilesProcessed.incrementAndGet();

        }
    }

    public void pauseThread(){
        pause = true;
    }

    public void resmueThread(){
        pause = false;
        synchronized (this){
            notifyAll();
        }
    }

}