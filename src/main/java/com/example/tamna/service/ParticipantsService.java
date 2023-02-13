package com.example.tamna.service;

import com.example.tamna.dto.BookingDataDto;
import com.example.tamna.dto.PostBookingDataDto;
import com.example.tamna.model.Booking;
import com.example.tamna.model.UserDto;
import com.example.tamna.mapper.ParticipantsMapper;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParticipantsService {

	private final Logger LOGGER = LoggerFactory.getLogger(ParticipantsService.class);

	private final ParticipantsMapper participantsMapper;
	private final UserService userService;

	public Date time() {
		long miliseconds = System.currentTimeMillis();
		return new Date(miliseconds);
	}

	// 나박스 회의실 예약자 insert
	public int insertApplicant(int bookingId, String userId) {
		return participantsMapper.insertParticipants(bookingId, userId, true);
	}

	// 회의실 예약 신청자와 멤버 구분하여 insert
	public void insertParticipants(int bookingId, List<UserDto> usersData, List<String> teamMate) {
		for (int i = 0; i < usersData.toArray().length; i++) {
			UserDto user = usersData.get(i);
			if (!teamMate.contains(user.getUserName())) {
				participantsMapper.insertParticipants(bookingId, user.getUserId(), true);
				;
			} else {
				participantsMapper.insertParticipants(bookingId, user.getUserId(), false);
			}
		}
	}

	// 나박스 시간 차이
	public boolean checkUsingTime(String getStartTime, String getEndTime) {
		int endTime = Integer.parseInt(getEndTime.substring(0, 2));
		int startTime = Integer.parseInt(getStartTime.substring(0, 2));
		int usingTime = endTime - startTime;
		return usingTime == 1;
	}

	// 회의실 횟수 한번 제한을 위한 체크
	public Map<Boolean, String> checkBookingUser(String roomType, String userId) {
		Map<Boolean, String> map = new HashMap<>();
		Date today = time();
		List<Booking> sameRoomTypeBookingCount = participantsMapper.selectBookingUser(today, roomType, userId, true);
		if (sameRoomTypeBookingCount.isEmpty()) {
			map.put(true, "success");
		} else {
			if (roomType.equals("nabox") && sameRoomTypeBookingCount.toArray().length == 1) {
				String endTime = sameRoomTypeBookingCount.get(0).getEndTime();
				String startTime = sameRoomTypeBookingCount.get(0).getStartTime();
				map.put(checkUsingTime(startTime, endTime), "add");
				return map;
			}
			map.put(false, "fail");
		}
		return map;
	}

	// 회의실 예약시, 동시간대 예약 체크
	public Set<String> checkUsingBooking(UserDto user, PostBookingDataDto postBookingDataDto) {
		Date today = time();
		List<BookingDataDto> usingCheck;

		if (postBookingDataDto.getRoomType().equals("meeting")) {
			String usersName = userService.changeString(user.getUserName(), postBookingDataDto.getTeamMate());
			usingCheck = participantsMapper.selectUsingUsers(today, user.getClasses(),
				postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime(), usersName);
		} else {
			usingCheck = participantsMapper.selectUsingOnlyUser(today, postBookingDataDto.getStartTime(),
				postBookingDataDto.getEndTime(), user.getUserId());
		}
		System.out.println("현재 회의실 사용중인 유저들: " + usingCheck);

		Set<String> usingUsers = new HashSet<>();
		if (!usingCheck.isEmpty()) {
			usingCheck.forEach(m -> usingUsers.add(m.getUserName()));
		}

        return usingUsers;
	}

};



