package com.krowd9.userexp

case class Config(
  contactsLimit: Int = 100,
  defaultLimit: Int = 25,
  dbPageSize: Int = 10
)