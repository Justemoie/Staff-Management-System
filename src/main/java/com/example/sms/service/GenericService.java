package com.example.sms.service;

import java.util.List;

public interface GenericService<T, RQ, ID> {
    List<T> getAll();

    T getById(ID id);

    T create(RQ requestEntity);

    T update(ID id, RQ requestEntity);

    void delete(ID id);
}
