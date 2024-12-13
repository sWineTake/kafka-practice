package com.group.server.job;

import com.group.server.domain.OutBoxTable;
import com.group.server.domain.OutBoxTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
		List<OutBoxTable> data = outBoxTableRepository.findAllBySendAndCreatedAtBeforeOrderById(true, oneDayAgo);

		// AWS S3에 파일 업로드


		// 메세지 리스트 삭제 -> todo) 실제 delete 쿼리 시에는 @Query 또는 myBatis 사용 예정
		outBoxTableRepository.deleteAll(data);

	}

	/*
	 * 1분 마다 실패된 메시지들을 재발행 - 재발을 어찌할까?
	 * 프로듀서 리트리 횟수 지정 -
	 * */
	@Scheduled(fixedDelay = 60000)
	public void batchReSend() {

		// 메시지 실패 케이스 조회
		LocalDateTime oneMinusAgo = LocalDateTime.now().minusMinutes(1);
		List<OutBoxTable> dataList = outBoxTableRepository.findAllBySendAndCreatedAtBeforeOrderById(false, oneMinusAgo);

		// 메세지 전송
		for (OutBoxTable data : dataList) {
			sendMessage(data);
		}


	}

	@Transactional
	public void sendMessage(OutBoxTable outBoxTable) {


	}

}
