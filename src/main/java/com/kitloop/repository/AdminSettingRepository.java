package com.kitloop.repository;

import com.kitloop.entity.AdminSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingRepository
        extends JpaRepository<AdminSetting, Long> {
}