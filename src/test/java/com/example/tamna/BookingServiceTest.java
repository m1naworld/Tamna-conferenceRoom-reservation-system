package com.example.tamna;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.tamna.dto.DetailBookingDataDto;
import com.example.tamna.mapper.BookingMapper;
import com.example.tamna.model.JoinBooking;
import com.example.tamna.service.BookingService;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

	@InjectMocks
	private BookingService bookingService;

	@Mock
	private BookingMapper bookingMapper;


	@Test
	void userIncludedBooking() {

		/* given - 데이터 준비 */
		String userId = "user1";

		List<Integer> bookingIdList = new ArrayList<>();
		bookingIdList.add(1);
		bookingIdList.add(3);
		bookingIdList.add(2);


		List<JoinBooking> myBookingList = new ArrayList<>();

		JoinBooking joinBooking1 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking1, "bookingId", 1);
		ReflectionTestUtils.setField(joinBooking1, "userId", "user1");
		ReflectionTestUtils.setField(joinBooking1, "userType", true);
		ReflectionTestUtils.setField(joinBooking1, "userName", "민아");
		JoinBooking joinBooking2 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking2, "bookingId", 1);
		ReflectionTestUtils.setField(joinBooking2, "userId", "user2");
		ReflectionTestUtils.setField(joinBooking2, "userType", false);
		ReflectionTestUtils.setField(joinBooking2, "userName", "수빈");
		JoinBooking joinBooking3 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking3, "bookingId", 1);
		ReflectionTestUtils.setField(joinBooking3, "userId", "user3");
		ReflectionTestUtils.setField(joinBooking3, "userType", false);
		ReflectionTestUtils.setField(joinBooking3, "userName", "현정");

		JoinBooking joinBooking4 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking4, "bookingId", 2);
		ReflectionTestUtils.setField(joinBooking4, "userId", "user1");
		ReflectionTestUtils.setField(joinBooking4, "userType", false);
		ReflectionTestUtils.setField(joinBooking4, "userName", "민아");
		JoinBooking joinBooking5 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking5, "bookingId", 2);
		ReflectionTestUtils.setField(joinBooking5, "userId", "user5");
		ReflectionTestUtils.setField(joinBooking5, "userType", true);
		ReflectionTestUtils.setField(joinBooking5, "userName", "무결");

		JoinBooking joinBooking6 = new JoinBooking();
		ReflectionTestUtils.setField(joinBooking6, "bookingId", 3);
		ReflectionTestUtils.setField(joinBooking6, "userId", "user1");
		ReflectionTestUtils.setField(joinBooking6, "userType", true);
		ReflectionTestUtils.setField(joinBooking6, "userName", "민아");

		myBookingList.add(joinBooking1);
		myBookingList.add(joinBooking2);
		myBookingList.add(joinBooking3);
		myBookingList.add(joinBooking4);
		myBookingList.add(joinBooking5);
		myBookingList.add(joinBooking6);

		/* stub - 가짜 객체 행동 정의 */
		when(bookingMapper.findMyBookingId(any(), eq(userId)))
			.thenReturn(bookingIdList);

		when(bookingMapper.findMyBookingData("'1','3','2'"))
			.thenReturn(myBookingList);

		/* when - 테스트 실행 */
		List<DetailBookingDataDto> result =  bookingService.todayMyBooking(userId);
		result.stream().forEach(a -> System.out.printf("%d / %s / %s / %s \n", a.getBookingId(),
			a.getApplicant().get("userId"), a.getApplicant().get("userName") , a.getParticipants()));

		/* then - 검증 */
		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0).getBookingId()).isEqualTo(1);
		assertThat(result.get(0).getApplicant().get("userId")).isEqualTo("user1");
		assertThat(result.get(0).getApplicant().get("userName")).isEqualTo("민아");
		assertThat(result.get(0).getParticipants().size()).isEqualTo(2);
		assertThat(result.get(0).getParticipants().get(0)).isEqualTo("수빈");
		assertThat(result.get(0).getParticipants().get(1)).isEqualTo("현정");

		assertThat(result.get(1).getApplicant().get("userName")).isEqualTo("무결");
		assertThat(result.get(1).getParticipants().get(0)).isEqualTo("민아");

		assertThat(result.get(2).getApplicant().get("userName")).isEqualTo("민아");
		assertThat(result.get(2).getParticipants().size()).isEqualTo(0);
	}
}
