package com.tiburcio.bicycles.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tiburcio.bicycles.entity.models.Bicycle;
import com.tiburcio.bicycles.entity.services.IBicycleService;

import jakarta.annotation.PostConstruct;

@RestController
public class BicycleController {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@PostConstruct
	public void init() throws IOException {
		Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(path);
	}

	@Autowired
	IBicycleService bicycleService;

	@GetMapping("/api/bicycles")
	public List<Bicycle> getAllBicycles() {
		return bicycleService.getAll();
	}

	@PostMapping("/api/bicycles")
	public ResponseEntity<String> post(@RequestParam("model") String model, @RequestParam("year") int year,
			@RequestParam("file") MultipartFile file) {

		Bicycle bicycle = new Bicycle(model, year, "");

		String fileName = "";

		try {
			// Save the file to the directory
			fileName = saveImage(file);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
		}

		bicycle.setImageFileName(fileName);
		bicycleService.post(bicycle);

		return ResponseEntity.ok("Image uploaded successfully: " + fileName);
	}

	private String saveImage(MultipartFile file) throws IOException {
		Path uploadPath = (Path) Paths.get(uploadDir);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		String OriginalFileName = file.getOriginalFilename();
		String fileName = "bicycle-" + System.currentTimeMillis() + "."
				+ OriginalFileName.substring(OriginalFileName.length() - 3);
		Path filePath = uploadPath.resolve(fileName);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		return fileName.toString();
	}

	@PutMapping("/api/bicycles/{id}")
	public void put(Bicycle bicycle, @PathVariable(value = "id") long id) {
		bicycleService.put(bicycle, id);
	}

	@DeleteMapping("/api/bicycles/{id}")
	public void delete(@PathVariable(value = "id") long id) {
		bicycleService.delete(id);
	}

	@GetMapping("/api/bicycles/images/{filename}")
	public ResponseEntity<Resource> getImage(@PathVariable String filename) {
		try {
			Path filePath = Paths.get(uploadDir).resolve(filename);
			Resource resource = new UrlResource(filePath.toUri());

			MediaType mediaType = null;

			switch (filename.substring(filename.length() - 3)) {
			case "gif":
				mediaType = MediaType.IMAGE_GIF;
				break;
			case "png":
				mediaType = MediaType.IMAGE_PNG;
				break;
			case "jpg":
			case "pge":
				mediaType = MediaType.IMAGE_JPEG;
				break;
			}

			if (resource.exists()) {
				return ResponseEntity.ok().contentType(mediaType).body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (MalformedURLException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
