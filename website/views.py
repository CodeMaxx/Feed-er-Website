from django.shortcuts import render,reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout, authenticate, login
from .models import *
# Create your views here.

## Show the main view
def signin(request):
	if request.method == "GET":
		user = request.user
		if user.is_authenticated():
			return HttpResponseRedirect('home')

		return render(request, "signin.html")

		
	elif request.method == "POST":

		username = request.POST['username']
		password = request.POST['password']

		user = authenticate(username=username, password=password)
		if user is None:
			return HttpResponseRedirect('signin')
		login(request, user)
		return HttpResponseRedirect('home')



## Signup view which takes data and creates the account
def signup(request):

	if request.method == "GET":
		return HttpResponseRedirect('signin')
	elif request.method == "POST":
		try:
			fullname = request.POST['fullname']
			username = request.POST['sUsername']
			password = request.POST['sPassword']
			isProf = request.POST['prof']
		except:
			return HttpResponseRedirect('signin')

		## We have the data
		userexists = User.objects.filter(username=username)
		if len(userexists) != 0:
			return HttpResponseRedirect('signin')

		newuser = User.objects.create(username=username, password=password)
		newuser.save()
		mtype = "PR" if isProf==True else "TA"
		
		newmember = Member.objects.create(
			user=newuser,
			fullname=fullname,
			mtype=mtype,
		)
		newmember.save()

		return HttpResponseRedirect('home')


@login_required
def home(request):
	## Anon users are shooed away
	if request.user.is_anonymous():
		return HttpResponseRedirect('signin')

	## get member and log out if student
	user = User.objects.get(username=request.user.username)
	member = Member.objects.get(user=user)

	if member.mtype == "ST":
		logout(request)
		return HttpResponse('You are not allowed to visit this page. Please use the app since you are a student.')

	return render(request, )

def signout(request):
	logout(request)
	return HttpResponseRedirect('signin')

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
