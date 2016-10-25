from django.shortcuts import render,reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout, authenticate, login
from .models import *
# Create your views here.

## Show the main view
## More like index page
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
			#return HttpResponseRedirect(reverse('website:signin'))
			return HttpResponse("User not found")
		login(request, user)
		return HttpResponseRedirect('home')


## Signup view which takes data and creates the account
## View is here
def signup(request):

	if request.method == "GET":
		return HttpResponseRedirect(reverse('website:signin'))
	elif request.method == "POST":
		try:
			fullname = request.POST['fullname']
			username = request.POST['sUsername']
			password = request.POST['sPassword']
			isProf = request.POST.get('prof')
		except:
			# return HttpResponseRedirect(reverse('website:signin'))
			return HttpResponse(request)

		## We have the data
		userexists = User.objects.filter(username=username)
		if len(userexists) != 0:
			#return HttpResponseRedirect(reverse('website:signin'))
			return HttpResponse("Used already exists. Please login")

		newuser = User.objects.create(username=username)
		newuser.set_password(password)
		newuser.save()
		mtype = "PR" if isProf==True else "TA"
		
		newmember = Member.objects.create(
			user=newuser,
			fullname=fullname,
			mtype=mtype,
		)
		newmember.save()
		return HttpResponseRedirect(reverse('website:signin'))



## Add views for Home Page and logic 
## Will contain different stuff for Admins and TAs/Profs
def home(request):
	## Anon users are shooed away
	if request.user.is_anonymous():
		return HttpResponseRedirect(reverse('website:signin'))

	## get member and log out if student
	user = User.objects.get(username=request.user.username)
	member = Member.objects.get(user=user)

	context = {
		'member':member,
	}

	if member.mtype == "ST":
		logout(request)
		return HttpResponse('You are not allowed to visit this page. Please use the app since you are a student.')

	return render(request, 'home.html', context)

## Signout and return to homepage
def signout(request):
	logout(request)
	return HttpResponseRedirect(reverse('website:signin'))

## Complete Registration
## Completes facebook signup
def completeReg(request):

	if request.method == "GET":	# the user is registered now
		user = request.user

		if not user.is_authenticated():
			return HttpResponse('Invalid Facebook redirect!')

		context = {
			'fullname':user.first_name + ' ' + user.last_name,
			'username':user.username,
		}
		return render(request,'complete-reg.html',context)

	elif request.method == "POST":

		try:
			fullname = request.POST['fullname']
			username = request.POST['username']
			isProf = request.POST.get('prof')
		except:
			return HttpResponse("Invalid fields!")

		user = User.objects.get(username=username)

		mtype = "PR" if isProf else "TA"

		member = Member.objects.filter(user=user)
		if len(member) != 0:
			return HttpResponse('This user already exists! Try logging in!')
		
		member = Member.objects.create(user=user,fullname=fullname,mtype=mtype)
		member.save()
		return HttpResponseRedirect(reverse('website:home'))

	else:
		return HttpResponse('Invalid request sent!')


### View courses, all courses for admins, Selected courses for Profs, or TAs
@login_required
def view_courses(request):

	member = Member.objects.get(user=request.user)

	if member.mtype == "ST":
		return HttpResponse("You cannot access the site. Please use the app.")

	## Admin, bring up all the courses
	elif member.mtype == "AD":
		all_courses = Course.objects.all()
		profs = []
		for course in all_courses:
			profs.append(course.members.filter(mtype="PR")[0])
		return render(request, 'view_courses.html', {'all_courses': all_courses, 'member':member, 'profs':profs})

	## Prof or TA, bring up the courses of the prof or TA
	else:
		all_courses = member.course_set.all()
		return render(request, 'view_courses.html', {'all_courses': all_courses, 'member':member})


### Add courses for Admins
@login_required
def add_courses(request):
	member = Member.objects.get(user=request.user)
	if request.method == "GET":
		print(member.mtype)
		if member.mtype == "ST":
			return HttpResponse("You cannot access the site. Please use the app.")
		
		profs = Member.objects.filter(mtype="PR")
		students = Member.objects.filter(mtype="ST")

		context = {
			'mtype':member.mtype,
			'profs':profs,
			'students':students,
		}

		return render(request,'add_courses.html',context)

	elif request.method == "POST":
		course_name = request.POST['name']
		code = request.POST['code']
		semester = int(request.POST['semester'])

		course = Course.objects.create(
			name=course_name,
			course_code=code,
			semester=semester
		)

		prof = request.POST.get('prof')
		students = request.POST.getlist('student')
		print(prof,students)

		prof = Member.objects.get(user__username=prof)
		course.members.add(prof)

		for student in students:
			stuObj = Member.objects.get(user__username=student)
			course.members.add(stuObj)

		course.save()

		# course.save()
		return HttpResponseRedirect(reverse('website:home'))


def assigns(request):
	return HttpResponse('Sign in page!')

def add_assigns(request):
	return HttpResponse('Sign in page!')

def course_detail(request,pk,year,sem):
	return HttpResponse('Sign in page!')
