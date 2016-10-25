from __future__ import unicode_literals

from django.contrib.auth.models import User
from django.db import models

# Create your models here.

CHOICES = (
    ('ST', 'Student'),
    ('PR', 'Professor'),
    ('TA', 'Teaching Assistant'),
    ('AD', 'Admin'),
)

## Member type which specifies member type
class Member(models.Model):
	user = models.OneToOneField(User, on_delete=models.CASCADE)
	fullname = models.CharField(max_length=30)
	# Member type
	mtype = models.CharField(choices=CHOICES, default="ST",max_length=25)
	
	def __str__(self):
		return self.fullname

## Course details
class Course(models.Model):
	name = models.TextField()
	memebers = models.ManyToManyField(Member)
	# assignments are added as foreign keys
	semester = models.IntegerField()
	year = models.DateField(auto_now_add=True,auto_now=False)
	course_code = models.CharField(max_length=5)
##############################################################################

## Contains the feedback for the current course
class Feedback(models.Model): 
	name = models.TextField()
	deadline = models.DateTimeField(blank=True)
	course = models.ForeignKey(Course)

## Feedback Questions and answers
class FeedbackQuestion(models.Model):
	question = models.TextField()
	feedback = models.ForeignKey(Feedback)

## Answer type to question given by users
class FeedbackRatingAnswer(models.Model):
	q = models.ForeignKey(FeedbackQuestion)
	rating = models.IntegerField(default=0)

###############################################################################
## Contains the assignment details for the current course
class Assignment(models.Model):
	name = models.TextField()
	description = models.TextField()
	deadline = models.DateTimeField(auto_now_add=True)
	course = models.ForeignKey(Course)

