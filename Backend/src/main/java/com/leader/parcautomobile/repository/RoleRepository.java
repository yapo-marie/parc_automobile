package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByName(String name);

	List<Role> findByNameIn(Collection<String> names);

	@Query("select distinct r from Role r left join fetch r.permissions")
	List<Role> findAllWithPermissions();

	@Query("select distinct r from Role r left join fetch r.permissions where r.id = :id")
	Optional<Role> findByIdWithPermissions(@Param("id") UUID id);
}
