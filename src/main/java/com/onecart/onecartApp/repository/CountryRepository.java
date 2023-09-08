package com.onecart.onecartApp.repository;

import com.onecart.onecartApp.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CountryRepository extends JpaRepository<Country, Long> {
}
