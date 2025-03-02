package com.example.sms.service;

import java.util.List;

public interface GenericService<T, R, I> {
    List<T> getAll();

    T getById(I id);

    T create(R requestEntity);

    T update(I id, R requestEntity);

    void delete(I id);
}
