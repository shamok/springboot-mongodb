package com.shayasit.springbootmongodb.collection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Document(collection = "person")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Person(@Id String personId, String firstName, String lastName, int age, List<String> hobbies,
                     List<Address> address) {
}
