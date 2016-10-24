from __future__ import unicode_literals

from django.contrib.auth.models import User
from django.db import models

# Create your models here.


class Members(models.Model):
	user = models.OneToOneField(User, on_delete=models.CASCADE)
	name = models.CharField(max_length=30)
	username = models.CharField(primary_key=True, max_length=10)
	email = models.EmailField()
	# password =
	# courses = models.
	type = models.CharField(max_length=1) # Maybe of 3 types - 'S' Student, 'T' TAs, 'P' Professors