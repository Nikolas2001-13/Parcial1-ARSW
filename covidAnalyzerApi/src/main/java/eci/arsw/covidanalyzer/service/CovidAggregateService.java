package eci.arsw.covidanalyzer.service;

import eci.arsw.covidanalyzer.model.Result;
import eci.arsw.covidanalyzer.model.ResultType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service("CovidAggregateService")
public class CovidAggregateService implements ICovidAggregateService{
    private List<Result> resultList = new CopyOnWriteArrayList<>();

    @Override
    public void aggregateResult(Result result, ResultType type){
        for (Result result1 : resultList){
            if (result1.equals(result)){
                result1.setResultType(type);
            }
        }
    }

    @Override
    public List<Result> getResult(ResultType type){
        List<Result> resyltsType = new CopyOnWriteArrayList<>();
        for (Result result1 : resultList){
            if (result1.getResultType().equals(type)){
                resyltsType.add(result1);
            }
        }
        return resyltsType;
    }

    @Override
    public void upsertPersonWithMultipleTests(UUID id, ResultType type){
        for (Result result1 : resultList){
            if (result1.getId().equals(id)){
                result1.setResultType(type);
            }
        }
    }
}