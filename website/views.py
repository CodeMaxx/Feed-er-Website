from django.shortcuts import render
from django.http import HttpResponse
# Create your views here.
def signin(request):
	return HttpResponse('Sign in page!')

def home(request):
	return HttpResponse('Sign in page!')

def logout(request):
	return HttpResponse('Sign in page!')

def courses(request):
	return HttpResponse('Sign in page!')

def add_courses(request):
	return HttpResponse('Sign in page!')

def assigns(request):
	return HttpResponse('Sign in page!')

def add_assigns(request):
	return HttpResponse('Sign in page!')

def course_detail(request,pk,year,sem):
	return HttpResponse('Sign in page!')
