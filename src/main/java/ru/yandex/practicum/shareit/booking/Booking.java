package ru.yandex.practicum.shareit.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.item.Item;
import ru.yandex.practicum.shareit.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDateTime start;

    @Column(name = "end_date")
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column
    private BookingStatus status;
}
