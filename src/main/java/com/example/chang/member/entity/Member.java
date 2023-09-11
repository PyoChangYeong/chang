package com.example.chang.member.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String pw;
    @Column(nullable = false)
    private String username;

    @Builder
    public Member(Long id,String email, String pw, String username) {
        this.id = id;
        this.email = email;
        this.pw = pw;
        this.username = username;
    }

    public static Member of(String email,String pw, String username){
        return Member.builder()
                .email(email).pw(pw).username(username).build();
    }
}
