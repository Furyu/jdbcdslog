package com.github.furyu.jdbcdslog.fluent

import com.github.furyu.jdbcdslog.fluent.DefaultWrites.JavaMapWrites

package object format {

  def writes[A](block: A => Any): JavaMapWrites[A] = new JavaMapWrites[A] {
    def writes(a: A) = block(a)
  }

}
