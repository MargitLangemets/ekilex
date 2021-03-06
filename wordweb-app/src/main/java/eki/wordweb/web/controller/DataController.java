package eki.wordweb.web.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eki.common.data.AppData;
import eki.common.web.AppDataHolder;
import eki.wordweb.constant.SystemConstant;
import eki.wordweb.service.FileService;
import eki.wordweb.service.SpeechSynthesisService;

@ConditionalOnWebApplication
@RestController
public class DataController implements SystemConstant {

	private static final Logger logger = LoggerFactory.getLogger(DataController.class);

	@Autowired
	private AppDataHolder appDataHolder;

	@Autowired
	private SpeechSynthesisService speechSynthesisService;

	@Autowired
	private FileService fileService;

	@GetMapping(value="/data/app", produces = "application/json;charset=UTF-8")
	public AppData getAppData(HttpServletRequest request) {
		return appDataHolder.getAppData(request, POM_PATH);
	}

	@PostMapping("/generate_voice")
	public String generateSoundFileUrl(@RequestParam String words) throws Exception {
		return speechSynthesisService.urlToSoundSource(words);
	}

	@GetMapping("/files/{fileId}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String fileId) {

		String fileName = "";
		Resource resource = null;
		try {
			Optional<Path> fileToServe = Files.find(
					Paths.get(System.getProperty("java.io.tmpdir")),
					1,
					(p,a) -> p.getFileName().toString().startsWith(fileId)).findFirst();
			if (fileToServe.isPresent()) {
				resource = new PathResource(fileToServe.get());
				fileName = fileToServe.get().getFileName().toString();
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}

	@GetMapping("/files/sounds/{fileName:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveSoundFile(@PathVariable String fileName) {

		Resource resource = fileService.getSoundFileAsResource(fileName);
		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}

	@GetMapping("/files/images/{fileName:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {

		Resource resource = fileService.getFileAsResource(fileName);
		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}

}
