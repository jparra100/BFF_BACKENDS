package cl.duoc.bancoxyz.customers.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record CustomerRequest(@NotBlank String name,@NotBlank @Email String email,@NotBlank String profile){}
