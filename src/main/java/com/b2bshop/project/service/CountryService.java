package com.b2bshop.project.service;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CountryService {

    private final EntityManager entityManager;

    public CountryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Map<String, Object>> getAllCountries() {
        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT country.id as countryId, country.name as countryName, country.code as countryCode " +
                "FROM Country as country";

        Query query = session.createQuery(hqlQuery);

        List<Object[]> results = query.list();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> countryMap = new HashMap<>();
            countryMap.put("id", result[0]);
            countryMap.put("countryName", result[1]);
            countryMap.put("countryCode", result[2]);
            resultList.add(countryMap);
        }

        return resultList;
    }
}
