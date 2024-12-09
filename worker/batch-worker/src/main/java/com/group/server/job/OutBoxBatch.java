package com.group.server.job;

import com.group.server.domain.OutBoxTable;
import com.group.server.domain.OutBoxTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
public class OutBoxBatch {

	private final OutBoxTableRepository outBoxTableRepository;

	/*
	* 1분 마다 out_box_table 에 성공케이스들을 조회하여 AWS S3 파일로 저장
	* */
	@Scheduled(cron = "0 30 2 * * *")
	@Transactional
	public void batchSaveS3() {

		// 메시지 정상케이스 조회
		LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
		List<OutBoxTable> results = outBoxTableRepository.findAllBySendAndCreatedAtBeforeOrderById(true, oneDayAgo);

		// AWS S3에 파일 업로드


		// 메세지 리스트 삭제 -> todo) 실제 delete 쿼리 시에는 @Query 또는 myBatis 사용 예정
		outBoxTableRepository.deleteAll(results);

	}



}
