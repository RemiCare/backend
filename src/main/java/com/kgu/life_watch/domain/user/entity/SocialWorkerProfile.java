package com.kgu.life_watch.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SocialWorkerProfile {

  @Id private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "socialWorkerProfile", cascade = CascadeType.ALL)
  private List<ElderlyProfile> assignedSeniors = new ArrayList<>();

  public void setUser(User user) {
    this.user = user;
  }

  public void addElderly(ElderlyProfile elderly) {
    assignedSeniors.add(elderly);
    elderly.setSocialWorkerProfile(this);
  }
}
