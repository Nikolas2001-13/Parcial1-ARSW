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

    /**
     * Metodo run el cual mientras este en pausa, los threads esperaran, y cuando no, se empezaran a añadir los resultados
     */
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

    /**
     * Si se pausa el thread, cambiara la variable pause a true
     */
    public void pauseThread(){
        pause = true;
    }

    /**
     * Se reanudaran todos los threads, cambiando la variable pause a false
     */
    public void resmueThread(){
        pause = false;
        synchronized (this){
            notifyAll();
        }
    }

}