package com.darauy.quark.repository;

import com.darauy.quark.entity.users.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
}
