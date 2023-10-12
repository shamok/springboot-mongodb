package com.shayasit.springbootmongodb.service;

import com.shayasit.springbootmongodb.collection.Person;
import com.shayasit.springbootmongodb.repository.PersonRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String save(Person person) {
        return personRepository.save(person).personId();
    }

    @Override
    public List<Person> getPersonStartsWith(String name) {
        return personRepository.findByFirstNameStartsWith(name);
    }

    @Override
    public void delete(String id) {
        personRepository.deleteById(id);
    }

    @Override
    public List<Person> getByPersonAge(Integer minAge, Integer maxAge) {
        return personRepository.findPersonByAgeBetween(minAge, maxAge);
    }

    @Override
    public Page<Person> search(String name, Integer minAge, Integer maxAge, String city, Pageable pageable) {

        Query query = new Query().with(pageable);
        List<Criteria> criteria = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            criteria.add(Criteria.where("firstName").regex(name, "i"));
        }

        if (minAge != null && maxAge != null) {
            criteria.add(Criteria.where("age").gte(minAge).lte(maxAge));
        }

        if (city != null && !city.isEmpty()) {
            criteria.add(Criteria.where("address.city").is(city));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        Page<Person> people = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Person.class),
                pageable,
                () -> mongoTemplate.count(query.skip(0).limit(0), Person.class)
        );
        return people;
    }

    @Override
    public List<Document> getOldestPersonByCity() {
        UnwindOperation unwindOperation
                = Aggregation.unwind("address");
        SortOperation sortOperation =
                Aggregation.sort(Sort.Direction.DESC, "age");
        GroupOperation groupOperation
                = Aggregation.group("address.city")
                .first(Aggregation.ROOT)
                .as("oldestPerson");

        Aggregation aggregation
                = Aggregation.newAggregation(unwindOperation, sortOperation, groupOperation);

        List<Document> person
                = mongoTemplate.aggregate(aggregation, Person.class, Document.class)
                .getMappedResults();
        return person;
    }

}
