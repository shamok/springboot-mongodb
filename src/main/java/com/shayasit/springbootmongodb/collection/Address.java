package com.shayasit.springbootmongodb.collection;

import lombok.Builder;

@Builder
public record Address(String address1, String address2, String city) {
}
