package com.leader.parcautomobile.repository;

import com.leader.parcautomobile.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

	boolean existsByEmailIgnoreCase(String email);

	@Query(
			"""
			select distinct u from User u
			left join fetch u.roles r
			left join fetch r.permissions
			where lower(u.email) = lower(:email)
			""")
	Optional<User> findByEmailWithRoles(@Param("email") String email);

	@Query(
			"""
			select distinct u from User u
			left join fetch u.roles r
			left join fetch r.permissions
			where u.id = :id
			""")
	Optional<User> findByIdWithRoles(@Param("id") UUID id);

	@Query(
			"""
			select distinct u.email from User u
			join u.roles r
			where r.name = :roleName
			and u.status = 'ACTIVE'
			and u.deletedAt is null
			order by u.email
			""")
	List<String> findActiveEmailsByRoleName(@Param("roleName") String roleName);

	@Query(
			"""
			select count(distinct u.id) from User u
			join u.roles r
			where r.id = :roleId
			and u.deletedAt is null
			""")
	long countActiveUsersWithRole(@Param("roleId") UUID roleId);
}
