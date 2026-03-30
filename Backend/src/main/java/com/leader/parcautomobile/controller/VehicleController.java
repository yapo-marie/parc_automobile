package com.leader.parcautomobile.controller;

import com.leader.parcautomobile.dto.vehicle.AvailabilityPatchRequest;
import com.leader.parcautomobile.dto.vehicle.CreateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.UpdateVehicleRequest;
import com.leader.parcautomobile.dto.vehicle.VehiclePageResponse;
import com.leader.parcautomobile.dto.vehicle.VehicleResponse;
import com.leader.parcautomobile.entity.VehicleAvailability;
import com.leader.parcautomobile.entity.VehicleCategory;
import com.leader.parcautomobile.entity.VehicleRecordStatus;
import com.leader.parcautomobile.service.VehicleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleService vehicleService;

	@GetMapping
	@PreAuthorize("hasAuthority('VEHICLE_READ')")
	public VehiclePageResponse list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) VehicleCategory category,
			@RequestParam(required = false) VehicleAvailability availability,
			@RequestParam(required = false) VehicleRecordStatus status,
			@PageableDefault(size = 20, sort = "plateNumber", direction = Sort.Direction.ASC)
					Pageable pageable) {
		return vehicleService.list(q, category, availability, status, pageable);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('VEHICLE_READ')")
	public VehicleResponse getById(@PathVariable UUID id) {
		return vehicleService.getById(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('VEHICLE_CREATE')")
	public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest body) {
		VehicleResponse created = vehicleService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
	public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest body) {
		return vehicleService.update(id, body);
	}

	@PatchMapping("/{id}/availability")
	@PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
	public VehicleResponse patchAvailability(
			@PathVariable UUID id, @Valid @RequestBody AvailabilityPatchRequest body) {
		return vehicleService.patchAvailability(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('VEHICLE_DELETE')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		vehicleService.softDelete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/photo")
	@PreAuthorize("hasAuthority('VEHICLE_UPDATE')")
	public VehicleResponse uploadPhoto(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
		return vehicleService.uploadPhoto(id, file);
	}

	@GetMapping("/photos/{filename:.+}")
	public ResponseEntity<Resource> getPhoto(@PathVariable String filename) {
		try {
			Path file = Paths.get("uploads", "vehicles", filename).normalize();
			Resource resource = new UrlResource(file.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				return ResponseEntity.notFound().build();
			}
			String ext = "";
			int dot = filename.lastIndexOf('.');
			if (dot >= 0) ext = filename.substring(dot + 1).toLowerCase();
			MediaType mediaType = switch (ext) {
				case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
				case "png" -> MediaType.IMAGE_PNG;
				case "webp" -> MediaType.parseMediaType("image/webp");
				default -> MediaType.APPLICATION_OCTET_STREAM;
			};
			return ResponseEntity.ok().contentType(mediaType).body(resource);
		}
		catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
