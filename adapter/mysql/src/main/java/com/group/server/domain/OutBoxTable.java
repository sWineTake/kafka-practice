package com.group.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "out_box_table")
public class OutBoxTable {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String message;

	@Column
	private boolean send;

	@Column
	private LocalDateTime createdAt;

	public static OutBoxTable create(String message, boolean send, LocalDateTime createdAt) {
		OutBoxTable outBoxTable = new OutBoxTable();
		outBoxTable.message = message;
		outBoxTable.send = send;
		outBoxTable.createdAt = createdAt;
		return outBoxTable;
	}

	public void updateSend(boolean result) {
		this.send = result;
	}
}
